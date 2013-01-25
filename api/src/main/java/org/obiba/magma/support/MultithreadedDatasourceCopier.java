package org.obiba.magma.support;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class MultithreadedDatasourceCopier {

  private static final Logger log = LoggerFactory.getLogger(MultithreadedDatasourceCopier.class);

  private static final int BUFFER_SIZE = 150;

  @SuppressWarnings({ "UnusedDeclaration", "ParameterHidesMemberVariable" })
  public static class Builder {

    MultithreadedDatasourceCopier copier = new MultithreadedDatasourceCopier();

    public Builder() {
    }

    public static Builder newCopier() {
      return new Builder();
    }

    public Builder withThreads(ThreadFactory factory) {
      copier.threadFactory = factory;
      return this;
    }

    public Builder withQueueSize(int size) {
      copier.bufferSize = size;
      return this;
    }

    public Builder withReaders(int readers) {
      copier.concurrentReaders = readers;
      return this;
    }

    public Builder withReaderListener(ReaderListener readerListener) {
      copier.readerListener = readerListener;
      return this;
    }

    public Builder withCopier(DatasourceCopier.Builder copier) {
      this.copier.copier = copier;
      return this;
    }

    public Builder from(ValueTable source) {
      copier.sourceTable = source;
      if(copier.destinationName == null) {
        copier.destinationName = source.getName();
      }
      return this;
    }

    public Builder to(Datasource destination) {
      copier.destinationDatasource = destination;
      return this;
    }

    public Builder as(String name) {
      copier.destinationName = name;
      return this;
    }

    public MultithreadedDatasourceCopier build() {
      return copier;
    }
  }

  public interface ReaderListener {

    void onRead(ValueSet valueSet, Value... values);

  }

  private ThreadFactory threadFactory;

  private int bufferSize = BUFFER_SIZE;

  private int concurrentReaders = 3;

  private DatasourceCopier.Builder copier = DatasourceCopier.Builder.newCopier();

  private ValueTable sourceTable;

  private String destinationName;

  private Datasource destinationDatasource;

  private VariableValueSource sources[];

  private Variable variables[];

  private final List<Future<?>> readers = Lists.newArrayList();

  private long entitiesToCopy = 0;

  private long entitiesCopied = 0;

  private int nextPercentIncrement = 0;

  private ReaderListener readerListener;

  private MultithreadedDatasourceCopier() {

  }

  public void copy() throws IOException {
    ThreadPoolExecutor executor = (ThreadPoolExecutor) (threadFactory == null
        ? Executors.newFixedThreadPool(concurrentReaders)
        : Executors.newFixedThreadPool(concurrentReaders, threadFactory));

    prepareVariables();

    // A queue containing all entity values available for writing to the destinationDatasource.
    BlockingQueue<VariableEntityValues> writeQueue = new LinkedBlockingDeque<VariableEntityValues>(bufferSize);

    if(copier.build().isCopyValues()) {

      if(copier.build().isIncremental()) {
        // wrap source table with IncrementalView
        sourceTable = IncrementalView.Factory.create(sourceTable, destinationDatasource);
      }

      // A queue containing all entities to read the values for.
      // Once this is empty, and all readers are done, then reading is over.
      BlockingQueue<VariableEntity> readQueue =
          new LinkedBlockingDeque<VariableEntity>(sourceTable.getVariableEntities());
      entitiesToCopy = readQueue.size();
      for(int i = 0; i < concurrentReaders; i++) {
        readers.add(executor.submit(new ConcurrentValueSetReader(readQueue, writeQueue)));
      }
    }
    try {
      write(writeQueue);
      checkReadersForException();
    } finally {
      log.debug("Finished multi-threaded copy. Submitted tasks {}, executed tasks {}", executor.getTaskCount(),
          executor.getCompletedTaskCount());
      executor.shutdownNow();
    }
  }

  private void write(BlockingQueue<VariableEntityValues> writeQueue) throws IOException {
    copyVariables();
    // The writers could also be concurrent, but dues to transaction isolation issues, it is currently ran
    // synchronously
    new ConcurrentValueSetWriter(writeQueue).run();
  }

  @SuppressWarnings("OverlyNestedMethod")
  private void checkReadersForException() {
    for(Future<?> reader : readers) {
      try {
        reader.get();
      } catch(InterruptedException e) {
        throw new RuntimeException(e);
      } catch(ExecutionException e) {
        Throwable cause = e.getCause();
        if(cause != null) {
          if(cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
          }
        }
        throw new RuntimeException(e);
      }
    }
  }

  private void prepareVariables() {
    List<VariableValueSource> list = Lists.newArrayList();
    List<Variable> vars = Lists.newArrayList();
    for(Variable variable : sourceTable.getVariables()) {
      list.add(sourceTable.getVariableValueSource(variable.getName()));
      vars.add(variable);
    }
    sources = list.toArray(new VariableValueSource[list.size()]);
    variables = vars.toArray(new Variable[list.size()]);
  }

  private void copyVariables() throws IOException {
    DatasourceCopier variableCopier = copier.build();
    if(variableCopier.isCopyMetadata()) {
      variableCopier.setCopyValues(false);
      variableCopier.copy(sourceTable, destinationName, destinationDatasource);
    }
  }

  private static class VariableEntityValues {

    private final ValueSet valueSet;

    private final Value[] values;

    private VariableEntityValues(ValueSet valueSet, Value... values) {
      this.valueSet = valueSet;
      this.values = values;
    }
  }

  private class ConcurrentValueSetReader implements Runnable {

    private final BlockingQueue<VariableEntity> readQueue;

    private final BlockingQueue<VariableEntityValues> writeQueue;

    private ConcurrentValueSetReader(BlockingQueue<VariableEntity> readQueue,
        BlockingQueue<VariableEntityValues> writeQueue) {
      this.readQueue = readQueue;
      this.writeQueue = writeQueue;
    }

    @SuppressWarnings("OverlyNestedMethod")
    @Override
    public void run() {
      try {
        VariableEntity entity = readQueue.poll();
        while(entity != null) {
          if(sourceTable.hasValueSet(entity)) {
            ValueSet valueSet = sourceTable.getValueSet(entity);
            Value[] values = new Value[sources.length];
            for(int i = 0; i < sources.length; i++) {
              values[i] = sources[i].getValue(valueSet);
            }
            log.debug("Enqueued entity {}", entity.getIdentifier());
            writeQueue.put(new VariableEntityValues(valueSet, values));
            if(readerListener != null) {
              readerListener.onRead(valueSet, values);
            }
          }
          entity = readQueue.poll();
        }
      } catch(InterruptedException ignored) {

      }
    }
  }

  private class ConcurrentValueSetWriter implements Runnable {

    private final BlockingQueue<VariableEntityValues> writeQueue;

    private ConcurrentValueSetWriter(BlockingQueue<VariableEntityValues> writeQueue) {
      this.writeQueue = writeQueue;
    }

    /**
     * Reads the next instance to write. This is a blocking operation. If nothing is left to write, this method will
     * return null.
     *
     * @return
     */
    VariableEntityValues next() {
      try {
        VariableEntityValues values = writeQueue.poll(1, TimeUnit.SECONDS);
        // If values is null, then it's either because we haven't done reading or we've finished reading
        while(values == null && !isReadCompleted()) {
          values = writeQueue.poll(1, TimeUnit.SECONDS);
        }
        return values;
      } catch(InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Returns true when all readers have finished submitting to the writeQueue. false otherwise.
     *
     * @return
     */
    private boolean isReadCompleted() {
      for(Future<?> reader : readers) {
        if(!reader.isDone()) {
          return false;
        }
      }
      return true;
    }

    @SuppressWarnings({ "ThrowFromFinallyBlock", "OverlyNestedMethod" })
    @Override
    public void run() {
      VariableEntityValues values = next();
      ValueTableWriter tableWriter =
          copier.build().innerValueTableWriter(sourceTable, destinationName, destinationDatasource);
      try {
        while(values != null) {
          ValueSetWriter writer = tableWriter.writeValueSet(values.valueSet.getVariableEntity());
          try {
            // Copy the ValueSet to the destinationDatasource
            log.debug("Dequeued entity {}", values.valueSet.getVariableEntity().getIdentifier());
            copier.build().copyValues(sourceTable, destinationName, values.valueSet, variables, values.values, writer);
          } finally {
            try {
              writer.close();
            } catch(IOException e) {
              throw new RuntimeException(e);
            }
          }
          entitiesCopied++;
          printProgress();
          values = next();
        }
      } finally {
        log.debug("Writer finished.");
        try {
          tableWriter.close();
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private void printProgress() {
      try {
        if(log.isInfoEnabled() && entitiesToCopy > 0) {
          int percentComplete = (int) (entitiesCopied / (double) entitiesToCopy * 100);
          if(percentComplete >= nextPercentIncrement) {
            log.info("Copy {}% complete.", percentComplete);
            nextPercentIncrement = percentComplete + 1;
          }
        }
      } catch(RuntimeException e) {
        // Ignore
      }
    }
  }
}

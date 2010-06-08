package org.obiba.magma.support;

import java.io.IOException;
import java.util.ArrayList;
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
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class MultithreadedDatasourceCopier {

  private static final Logger log = LoggerFactory.getLogger(MultithreadedDatasourceCopier.class);

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
      this.copier.bufferSize = size;
      return this;
    }

    public Builder withReaders(int readers) {
      this.copier.concurrentReaders = readers;
      return this;
    }

    public Builder withCopier(DatasourceCopier.Builder copier) {
      this.copier.copier = copier;
      return this;
    }

    public Builder from(ValueTable source) {
      this.copier.source = source;
      if(this.copier.destinationName == null) {
        this.copier.destinationName = source.getName();
      }
      return this;
    }

    public Builder to(Datasource destination) {
      this.copier.destination = destination;
      return this;
    }

    public Builder as(String name) {
      this.copier.destinationName = name;
      return this;
    }

    public MultithreadedDatasourceCopier build() {
      return copier;
    }
  }

  private ThreadFactory threadFactory;

  private int bufferSize = 150;

  private int concurrentReaders = 3;

  private DatasourceCopier.Builder copier;

  private ValueTable source;

  private String destinationName;

  private Datasource destination;

  private VariableValueSource sources[];

  private Variable variables[];

  private List<Future<?>> readers = Lists.newArrayList();

  private MultithreadedDatasourceCopier() {

  }

  public void copy() throws IOException {
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(concurrentReaders, threadFactory);

    prepareVariables();

    BlockingQueue<VariableEntityValues> writeQueue = new LinkedBlockingDeque<VariableEntityValues>(bufferSize);

    // Partition the list of ValueSets to read: produces one list of ValueSet per requested reader thread
    List<VariableEntity> entities = ImmutableList.copyOf(source.getVariableEntities());
    if(entities.size() > 0) {
      List<List<VariableEntity>> partitions = Lists.partition(entities, entities.size() / concurrentReaders);
      for(List<VariableEntity> partition : partitions) {
        ConcurrentValueSetReader reader = new ConcurrentValueSetReader(partition, writeQueue);
        readers.add(executor.submit(reader));
      }
    }
    try {
      write(writeQueue);
      checkReadersForException();
    } finally {
      log.debug("Finished multi-threaded copy. Submited tasks {}, executed tasks {}", executor.getTaskCount(), executor.getCompletedTaskCount());
      executor.shutdownNow();
    }
  }

  private void write(BlockingQueue<VariableEntityValues> writeQueue) throws IOException {
    copyVariables();
    // The writers could also be concurrent, but dues to transaction isolation issues, it is currently ran
    // synchronously
    new ConcurrentValueSetWriter(writeQueue).run();
  }

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

  /**
   * Returns true when all readers have finished submitting to the writeQueue. false otherwise.
   * @return
   */
  private boolean isReadCompleted() {
    for(Future<?> reader : readers) {
      if(reader.isDone() == false) {
        return false;
      }
    }
    return true;
  }

  private void prepareVariables() {
    ArrayList<VariableValueSource> sources = Lists.newArrayList();
    ArrayList<Variable> vars = Lists.newArrayList();
    for(Variable variable : source.getVariables()) {
      sources.add(source.getVariableValueSource(variable.getName()));
      vars.add(variable);
    }
    this.sources = sources.toArray(new VariableValueSource[sources.size()]);
    this.variables = vars.toArray(new Variable[sources.size()]);
  }

  private void copyVariables() throws IOException {
    DatasourceCopier variableCopier = copier.build();
    if(variableCopier.isCopyMetadata()) {
      variableCopier.setCopyValues(false);
      variableCopier.copy(source, destinationName, destination);
    }
  }

  private class VariableEntityValues {

    private final ValueSet valueSet;

    private final Value[] values;

    private VariableEntityValues(ValueSet valueSet, Value[] values) {
      this.valueSet = valueSet;
      this.values = values;
    }
  }

  private class ConcurrentValueSetReader implements Runnable {

    private final List<VariableEntity> entities;

    private final BlockingQueue<VariableEntityValues> writeQueue;

    private ConcurrentValueSetReader(List<VariableEntity> entities, BlockingQueue<VariableEntityValues> writeQueue) {
      this.entities = entities;
      this.writeQueue = writeQueue;
      log.info("Will read {} valueSets", entities.size());
    }

    @Override
    public void run() {
      try {
        for(VariableEntity entity : entities) {
          ValueSet valueSet = source.getValueSet(entity);
          Value[] values = new Value[sources.length];
          for(int i = 0; i < sources.length; i++) {
            values[i] = sources[i].getValue(valueSet);
          }
          log.info("Enqueued entity {}", entity.getIdentifier());
          writeQueue.put(new VariableEntityValues(valueSet, values));
        }
      } catch(InterruptedException e) {

      }
    }
  }

  private class ConcurrentValueSetWriter implements Runnable {

    private final BlockingQueue<VariableEntityValues> writeQueue;

    private ConcurrentValueSetWriter(BlockingQueue<VariableEntityValues> writeQueue) {
      this.writeQueue = writeQueue;
      log.info("Writer created. Queue size is {}", writeQueue.size());
    }

    /**
     * Reads the next instance to write. This is a blocking operation. If nothing is left to write, this method will
     * return null.
     * @return
     */
    VariableEntityValues next() {
      try {
        VariableEntityValues values = writeQueue.poll(1, TimeUnit.SECONDS);
        // If values is null, then it's either because we haven't done reading or we've finished reading
        while(values == null && isReadCompleted() == false) {
          values = writeQueue.poll(1, TimeUnit.SECONDS);
        }
        return values;
      } catch(InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void run() {
      VariableEntityValues values = next();
      ValueTableWriter tableWriter = copier.build().createValueTableWriter(source, destinationName, destination);
      try {
        while(values != null) {
          ValueSetWriter writer = tableWriter.writeValueSet(values.valueSet.getVariableEntity());
          try {
            // Copy the ValueSet to the destination
            log.info("Dequeued entity {}", values.valueSet.getVariableEntity().getIdentifier());
            copier.build().copy(source, destinationName, values.valueSet, variables, values.values, writer);
          } finally {
            try {
              writer.close();
            } catch(IOException e) {
              throw new RuntimeException(e);
            }
          }
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
  }
}

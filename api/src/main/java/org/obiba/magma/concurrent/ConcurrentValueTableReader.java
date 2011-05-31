package org.obiba.magma.concurrent;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ConcurrentValueTableReader {

  private static final Logger log = LoggerFactory.getLogger(ConcurrentValueTableReader.class);

  public static class Builder {

    ConcurrentValueTableReader reader = new ConcurrentValueTableReader();

    public Builder() {
    }

    public static Builder newReader() {
      return new Builder();
    }

    public Builder withThreads(ThreadFactory factory) {
      reader.threadFactory = factory;
      return this;
    }

    public Builder withReaders(int readers) {
      this.reader.concurrentReaders = readers;
      return this;
    }

    public Builder from(ValueTable source) {
      this.reader.valueTable = source;
      return this;
    }

    public Builder to(ConcurrentReaderCallback callback) {
      this.reader.callback = callback;
      return this;
    }

    public Builder variables(Iterable<Variable> variables) {
      this.reader.variables = variables;
      return this;
    }

    public Builder entities(Iterable<VariableEntity> entities) {
      this.reader.entities = entities;
      return this;
    }

    public ConcurrentValueTableReader build() {
      return reader;
    }
  }

  public interface ConcurrentReaderCallback {

    /**
     * Called before reading starts. The method is provided with the list of entities and variables that will be read
     * concurrently.
     * 
     * @param entities entities that will be read
     * @param variables variables that will be read
     */
    public void onBegin(List<VariableEntity> entities, Variable[] variables);

    /**
     * Called when a set of {@code Value} has been read and is ready to be written. This method is not called
     * concurrently. Implementations are not required to be threadsafe.
     * 
     * @param entity the {@code VariableEntity} that has been read
     * @param variables the {@code Variable} instances for which the values were read
     * @param values the {@code Value} instances, one per variable
     */
    public void onValues(VariableEntity entity, Variable[] variables, Value[] values);

    /**
     * Called when all entities have been read.
     */
    public void onComplete();

  }

  private ThreadFactory threadFactory;

  private ConcurrentReaderCallback callback;

  private int concurrentReaders = Runtime.getRuntime().availableProcessors() * 2;

  private ValueTable valueTable;

  private Iterable<Variable> variables;

  private Iterable<VariableEntity> entities;

  private ConcurrentValueTableReader() {

  }

  public void read() {
    ThreadPoolExecutor executor = threadFactory != null ? (ThreadPoolExecutor) Executors.newFixedThreadPool(concurrentReaders, threadFactory) : (ThreadPoolExecutor) Executors.newFixedThreadPool(concurrentReaders);

    Variable[] variables = prepareVariables();
    VariableValueSource[] sources = prepareSources(variables);

    List<VariableEntity> entitiesToCopy = ImmutableList.copyOf(entities == null ? valueTable.getVariableEntities() : entities);

    // A queue containing all entities to read the values for. Once this is empty, and all readers are done, then
    // reading is over.
    BlockingQueue<VariableEntity> readQueue = new LinkedBlockingDeque<VariableEntity>(entitiesToCopy);
    BlockingQueue<VariableEntityValues> writeQueue = new LinkedBlockingDeque<VariableEntityValues>();

    try {
      callback.onBegin(entitiesToCopy, variables);
      List<Future<?>> readers = Lists.newArrayList();
      if(entitiesToCopy.size() > 0) {
        for(int i = 0; i < concurrentReaders; i++) {
          readers.add(executor.submit(new ConcurrentValueSetReader(sources, readQueue, writeQueue)));
        }

        VariableEntityValues values = writeQueue.poll();
        while(values != null || isReadCompleted(readers) == false) {
          if(values != null) {
            callback.onValues(values.getEntity(), variables, values.getValues());
          }
          values = writeQueue.poll();
        }
      }
      callback.onComplete();
      waitForReaders(readers);
    } finally {
      executor.shutdownNow();
    }
  }

  /**
   * Returns true when all readers have finished submitting to the writeQueue. false otherwise.
   * @return
   */
  private boolean isReadCompleted(List<Future<?>> readers) {
    for(Future<?> reader : readers) {
      if(reader.isDone() == false) {
        return false;
      }
    }
    return true;
  }

  private void waitForReaders(List<Future<?>> readers) {
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
        throw new RuntimeException(e.getCause());
      }
    }
  }

  private VariableValueSource[] prepareSources(Variable[] variables) {
    VariableValueSource[] sources = new VariableValueSource[variables.length];
    for(int i = 0; i < variables.length; i++) {
      sources[i] = valueTable.getVariableValueSource(variables[i].getName());
    }
    return sources;
  }

  private Variable[] prepareVariables() {
    return Iterables.toArray(variables == null ? valueTable.getVariables() : variables, Variable.class);
  }

  public static class VariableEntityValues {

    private final VariableEntity entity;

    private final Value[] values;

    private VariableEntityValues(VariableEntity entity, Value[] values) {
      this.entity = entity;
      this.values = values;
    }

    public VariableEntity getEntity() {
      return entity;
    }

    public Value[] getValues() {
      return values;
    }
  }

  private class ConcurrentValueSetReader implements Runnable {

    private final VariableValueSource[] sources;

    private final BlockingQueue<VariableEntity> readQueue;

    private final BlockingQueue<VariableEntityValues> writeQueue;

    private ConcurrentValueSetReader(VariableValueSource[] sources, BlockingQueue<VariableEntity> readQueue, BlockingQueue<VariableEntityValues> writeQueue) {
      this.sources = sources;
      this.readQueue = readQueue;
      this.writeQueue = writeQueue;
    }

    @Override
    public void run() {
      try {
        VariableEntity entity = readQueue.poll();
        while(entity != null) {
          if(valueTable.hasValueSet(entity)) {
            ValueSet valueSet = valueTable.getValueSet(entity);
            Value[] values = new Value[sources.length];
            for(int i = 0; i < sources.length; i++) {
              values[i] = sources[i].getValue(valueSet);
            }
            log.debug("Read entity {}", entity.getIdentifier());
            writeQueue.put(new VariableEntityValues(entity, values));
          }
          entity = readQueue.poll();
        }
      } catch(InterruptedException e) {

      }
    }
  }

}

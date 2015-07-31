package org.obiba.magma.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;

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

@SuppressWarnings("UnusedDeclaration")
public class ConcurrentValueTableReader {

  private static final int BUFFER_SIZE = 200;

  private static final Logger log = LoggerFactory.getLogger(ConcurrentValueTableReader.class);

  private boolean ignoreReadErrors = false;

  private ThreadFactory threadFactory;

  private ConcurrentReaderCallback callback;

  private int nbConcurrentReaders = Runtime.getRuntime().availableProcessors() * 2;

  private ValueTable valueTable;

  private Iterable<Variable> variablesFilter;

  private Iterable<VariableEntity> entitiesFilter;

  private Variable[] variables;

  private BlockingQueue<VariableEntityValues> writeQueue;

  private int bufferSize = BUFFER_SIZE;

  private ConcurrentValueTableReader() {
  }

  public void read() {
    ExecutorService executorService = threadFactory == null
        ? Executors.newFixedThreadPool(nbConcurrentReaders)
        : Executors.newFixedThreadPool(nbConcurrentReaders, threadFactory);

    variables = Iterables
        .toArray(variablesFilter == null ? valueTable.getVariables() : variablesFilter, Variable.class);

    VariableValueSource[] variableValueSources = getVariableValueSources();
    List<VariableEntity> entities = ImmutableList
        .copyOf(entitiesFilter == null ? valueTable.getVariableEntities() : entitiesFilter);

    // A queue containing all entities to read the values for.
    // Once this is empty, and all readers are done, then  reading is over.
    BlockingQueue<VariableEntity> readQueue = new LinkedBlockingDeque<>(entities);
    writeQueue = new LinkedBlockingDeque<>(bufferSize);

    try {
      callback.onBegin(entities, variables);
      List<Future<?>> readers = entities.isEmpty()
          ? new ArrayList<Future<?>>()
          : concurrentRead(executorService, variableValueSources, readQueue);
      callback.onComplete();
      waitForReaders(readers);
    } finally {
      executorService.shutdownNow();
    }
  }

  private List<Future<?>> concurrentRead(ExecutorService executorService, VariableValueSource[] variableValueSources,
      BlockingQueue<VariableEntity> readQueue) {
    List<Future<?>> readers = Lists.newArrayList();
    for(int i = 0; i < nbConcurrentReaders; i++) {
      readers.add(executorService.submit(new ConcurrentValueSetReader(variableValueSources, readQueue, writeQueue)));
    }
    while(!isReadCompleted(readers)) {
      flushQueue();
    }
    // Flush remaining values if any
    // This is necessary due to a race condition between isReadComplete() and readers appending to the write queue
    flushQueue();
    return readers;
  }

  private VariableValueSource[] getVariableValueSources() {
    VariableValueSource[] variableValueSources = new VariableValueSource[variables.length];
    for(int i = 0; i < variables.length; i++) {
      variableValueSources[i] = valueTable.getVariableValueSource(variables[i].getName());
    }
    return variableValueSources;
  }

  private void flushQueue() {
    VariableEntityValues values;

    while((values = writeQueue.poll()) != null) {
      callback.onValues(values.getEntity(), variables, values.getValues());
      log.trace("write onCallback for entity {}", values.getEntity().getIdentifier());
    }
  }

  /**
   * Returns true when all readers have finished submitting to the writeQueue. false otherwise.
   *
   * @return
   */
  private boolean isReadCompleted(Iterable<Future<?>> readers) {
    for(Future<?> reader : readers) {
      if(!reader.isDone()) {
        return false;
      }
    }
    return true;
  }

  private void waitForReaders(Iterable<Future<?>> readers) {
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
        throw new RuntimeException(cause);
      }
    }
  }

  private VariableValueSource[] prepareSources(
      @SuppressWarnings("ParameterHidesMemberVariable") Variable... variables) {
    VariableValueSource[] sources = new VariableValueSource[variables.length];
    for(int i = 0; i < variables.length; i++) {
      sources[i] = valueTable.getVariableValueSource(variables[i].getName());
    }
    return sources;
  }

  private static class VariableEntityValues {

    private final VariableEntity entity;

    private final Value[] values;

    private VariableEntityValues(VariableEntity entity, Value... values) {
      this.entity = entity;
      this.values = values;
    }

    public VariableEntity getEntity() {
      return entity;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP")
    public Value[] getValues() {
      return values;
    }
  }

  private class ConcurrentValueSetReader implements Runnable {

    private final VariableValueSource[] sources;

    private final BlockingQueue<VariableEntity> readQueue;

    private final BlockingQueue<VariableEntityValues> writeQueue;

    private ConcurrentValueSetReader(VariableValueSource[] sources, BlockingQueue<VariableEntity> readQueue,
        BlockingQueue<VariableEntityValues> writeQueue) {
      this.sources = sources;
      this.readQueue = readQueue;
      this.writeQueue = writeQueue;
    }

    @Override
    public void run() {
      try {
        VariableEntity entity = readQueue.poll();
        while(entity != null && !callback.isCancelled()) {
          if(valueTable.hasValueSet(entity)) {
            log.trace("Read entity {}", entity.getIdentifier());
            writeQueue.put(new VariableEntityValues(entity, readValues(entity)));
          }
          entity = readQueue.poll();
        }
      } catch(InterruptedException e) {
        // do nothing
      }
    }

    private Value[] readValues(VariableEntity entity) {
      ValueSet valueSet = valueTable.getValueSet(entity);
      Value[] values = new Value[sources.length];
      for(int i = 0; i < sources.length; i++) {
        try {
          values[i] = sources[i].getValue(valueSet);
        } catch(RuntimeException e) {
          log.debug("Read exception", e);
          if(ignoreReadErrors) {
            values[i] = sources[i].getValueType().nullValue();
          } else {
            throw e;
          }
        }
      }
      return values;
    }
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public interface ConcurrentReaderCallback {

    /**
     * Called before reading starts. The method is provided with the list of entities and variables that will be read
     * concurrently.
     *
     * @param entities entities that will be read
     * @param variables variables that will be read
     */
    void onBegin(List<VariableEntity> entities, Variable... variables);

    /**
     * Called when a set of {@code Value} has been read and is ready to be written. This method is not called
     * concurrently. Implementations are not required to be threadsafe.
     *
     * @param entity the {@code VariableEntity} that has been read
     * @param variables the {@code Variable} instances for which the values were read
     * @param values the {@code Value} instances, one per variable
     */
    void onValues(VariableEntity entity, Variable[] variables, Value... values);

    /**
     * Called when all entities have been read.
     */
    void onComplete();

    /**
     * Request for readers to cancel prematurely.
     *
     * @return
     */
    boolean isCancelled();

  }

  @SuppressWarnings("ParameterHidesMemberVariable")
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
      reader.nbConcurrentReaders = readers;
      return this;
    }

    public Builder withBufferSize(int bufferSize) {
      reader.bufferSize = bufferSize;

      return this;
    }

    public Builder from(ValueTable source) {
      reader.valueTable = source;
      return this;
    }

    public Builder to(ConcurrentReaderCallback callback) {
      reader.callback = callback;
      return this;
    }

    public Builder variablesFilter(Iterable<Variable> variablesFilter) {
      reader.variablesFilter = variablesFilter;
      return this;
    }

    public Builder entitiesFilter(Iterable<VariableEntity> entitiesFilter) {
      reader.entitiesFilter = entitiesFilter;
      return this;
    }

    public Builder ignoreReadErrors() {
      reader.ignoreReadErrors = true;
      return this;
    }

    public ConcurrentValueTableReader build() {
      return reader;
    }
  }
}

package org.obiba.magma.support;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceCopierProgressListener;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.MultiplexingValueTableWriter.MultiplexedValueSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@SuppressWarnings("UnusedDeclaration")
public class DatasourceCopier {

  private static final Logger log = LoggerFactory.getLogger(DatasourceCopier.class);

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private DatasourceCopier copier = new DatasourceCopier();

    public Builder() {
    }

    public static Builder newCopier() {
      return new Builder();
    }

    public static Builder newCopier(DatasourceCopier copier) {
      Builder b = new Builder();
      b.copier = copier;
      return b;
    }

    public Builder dontCopyValues() {
      copier.copyValues = false;
      return this;
    }

    public Builder dontCopyMetadata() {
      copier.copyMetadata = false;
      return this;
    }

    public Builder dontCopyNullValues() {
      copier.copyNullValues = false;
      return this;
    }

    public Builder copyNullValues(boolean shouldCopy) {
      copier.copyNullValues = shouldCopy;
      return this;
    }

    public Builder withListener(DatasourceCopyEventListener listener) {
      if(listener == null) throw new IllegalArgumentException("listener cannot be null");
      copier.listeners.add(listener);
      return this;
    }

    public Builder withProgressListener(@Nullable DatasourceCopierProgressListener progressListener) {
      if(progressListener != null) withListener(new DatasourceCopyProgressListener(progressListener));
      return this;
    }

    public Builder withLoggingListener() {
      copier.listeners.add(new LoggingListener());
      return this;
    }

    public Builder withThroughtputListener() {
      copier.listeners.add(new ThroughputListener());
      return this;
    }

    public Builder withVariableTransformer(VariableTransformer transformer) {
      copier.variableTransformer = transformer;
      return this;
    }

    public Builder withMultiplexingStrategy(MultiplexingStrategy strategy) {
      copier.multiplexer = strategy;
      return this;
    }

    public DatasourceCopier build() {
      return new DatasourceCopier(copier);
    }

    private static class DatasourceCopyProgressListener implements DatasourceCopyValueSetEventListener {

      private final DatasourceCopierProgressListener progressListener;

      private long entitiesToCopy = 0;

      private long entitiesCopied = 0;

      private int nextPercentIncrement = 0;

      private DatasourceCopyProgressListener(DatasourceCopierProgressListener progressListener) {
        this.progressListener = progressListener;
      }

      @Override
      public void onValueSetCopy(ValueTable source, ValueSet valueSet) {
        if (entitiesToCopy == 0) {
          entitiesToCopy = source.getValueSetCount();
        }
      }

      @Override
      public void onValueSetCopied(ValueTable source, ValueSet valueSet, String... tables) {
        entitiesCopied++;
        printProgress(source);
      }

      private void printProgress(ValueTable source) {
        try {
          if(entitiesToCopy > 0) {
            int percentComplete = (int) (entitiesCopied / (double) entitiesToCopy * 100);
            if(percentComplete >= nextPercentIncrement) {
              log.info("Copy {}% complete.", percentComplete);
              progressListener.status(source.getName(), entitiesCopied, entitiesToCopy, percentComplete);
              nextPercentIncrement = percentComplete + 1;
            }
          }
        } catch(RuntimeException e) {
          // Ignore
        }
      }
    }
  }

  private boolean copyNullValues = true;

  private boolean copyMetadata = true;

  private boolean copyValues = true;

  private Collection<DatasourceCopyEventListener> listeners = new LinkedList<>();

  private VariableTransformer variableTransformer = new NoOpTransformer();

  private MultiplexingStrategy multiplexer = null;

  private DatasourceCopier() {
  }

  private DatasourceCopier(DatasourceCopier other) {
    copyNullValues = other.copyNullValues;
    copyMetadata = other.copyMetadata;
    copyValues = other.copyValues;
    listeners = ImmutableList.copyOf(other.listeners);
    variableTransformer = other.variableTransformer;
    multiplexer = other.multiplexer;
  }

  public void copy(String sourceDatasource, String destinationDatasource) throws IOException {
    copy(MagmaEngine.get().getDatasource(sourceDatasource), MagmaEngine.get().getDatasource(destinationDatasource));
  }

  public void copy(Datasource source, Datasource destination) throws IOException {
    if(source == destination) {
      // Don't copyMetadata on itself! The caller probably didn't want to really do this, did they?
      log.warn(
          "Invoked Datasource to Datasource copyMetadata with the same Datasource instance as sourceDatasource and destinationDatasource. " +
              "Nothing copied to or from Datasource '{}'.", source.getName());
      return;
    }

    Set<ValueTable> tables = source.getValueTables();
    int nbTables = tables.size();
    log.info("Copying Datasource '{}' to '{}' ({} tables)", source.getName(), destination.getName(), nbTables);
    int i = 1;
    for(ValueTable table : tables) {
      log.debug("Copy table {} / {}", i++, nbTables);
      copy(table, destination);
    }
  }

  public void copy(ValueTable sourceTable, Datasource destination) throws IOException {
    copy(sourceTable, sourceTable.getName(), destination);
  }

  public void copy(ValueTable sourceTable, String destinationTableName, Datasource destination) throws IOException {
    log.info("Copying ValueTable '{}' to '{}.{}' (copyMetadata={}, copyValues={}).", sourceTable.getName(),
        destination.getName(), destinationTableName, copyMetadata, copyValues);
    Stopwatch stopwatch = null;
    if(log.isDebugEnabled()) {
      stopwatch = Stopwatch.createStarted();
      log.debug("  --> {} variables, {} valueSets", sourceTable.getVariableCount(), sourceTable.getValueSetCount());
    }
    try(ValueTableWriter tableWriter = innerValueTableWriter(sourceTable, destinationTableName, destination)) {
      copy(sourceTable, destination.getValueTable(destinationTableName), tableWriter);
    }
    if(log.isDebugEnabled()) {
      //noinspection ConstantConditions
      log.debug("Copied ValueTable '{}' n {}", sourceTable.getName(), stopwatch.stop());
    }
  }

  private void copy(ValueTable sourceTable, ValueTable destinationTable, ValueTableWriter tableWriter)
      throws IOException {
    copyMetadata(sourceTable, destinationTable.getName(), tableWriter);
    copyValues(sourceTable, destinationTable, tableWriter);
  }

  private void copyValues(ValueTable sourceTable, ValueTable destinationTable, ValueTableWriter tableWriter)
      throws IOException {
    if(!copyValues) return;

    log.debug("Copy values from {} {}", sourceTable.getClass(), sourceTable.getName());
    for(ValueSet valueSet : sourceTable.getValueSets()) {
      try(ValueSetWriter valueSetWriter = tableWriter.writeValueSet(valueSet.getVariableEntity())) {
        copyValues(sourceTable, valueSet, destinationTable.getName(), valueSetWriter);
      }
    }
  }

  public void copyValues(ValueTable sourceTable, ValueSet valueSet, String destinationTableName,
      ValueSetWriter valueSetWriter) {
    if(!copyValues) return;
    notifyListeners(sourceTable, valueSet, false);
    for(Variable variable : sourceTable.getVariables()) {
      Value value = sourceTable.getValue(variable, valueSet);
      if(!value.isNull() || copyNullValues) {
        valueSetWriter.writeValue(variableTransformer.transform(variable), value);
      }
    }
    if(valueSetWriter instanceof MultiplexedValueSetWriter) {
      Set<String> tables = ((MultiplexedValueSetWriter) valueSetWriter).getTables();
      notifyListeners(sourceTable, valueSet, true, tables.toArray(new String[tables.size()]));
    } else {
      notifyListeners(sourceTable, valueSet, true, destinationTableName);
    }
  }

  public void copyMetadata(ValueTable sourceTable, String destinationTableName, ValueTableWriter tableWriter)
      throws IOException {
    if(!copyMetadata) return;
    try(VariableWriter variableWriter = tableWriter.writeVariables()) {
      copyMetadata(sourceTable, variableWriter);
    }
  }

  public void copyMetadata(ValueTable sourceTable, VariableWriter variableWriter) {
    if(!copyMetadata) return;
    for(Variable variable : sourceTable.getVariables()) {
      notifyListeners(variable, false);
      variableWriter.writeVariable(variableTransformer.transform(variable));
      notifyListeners(variable, true);
    }
  }

  void copyValues(ValueTable source, String tableName, ValueSet valueSet, Variable[] variables, Value[] values,
      ValueSetWriter vsw) {
    if(!copyValues) return;
    notifyListeners(source, valueSet, false);
    for(int i = 0; i < variables.length; i++) {
      Value value = values[i];
      if(!value.isNull() || copyNullValues) {
        Variable variable = variables[i];
        vsw.writeValue(variableTransformer.transform(variable), value);
      }
    }
    if(vsw instanceof MultiplexedValueSetWriter) {
      Set<String> tables = ((MultiplexedValueSetWriter) vsw).getTables();
      notifyListeners(source, valueSet, true, tables.toArray(new String[tables.size()]));
    } else {
      notifyListeners(source, valueSet, true, tableName);
    }
  }

  public ValueTableWriter createValueTableWriter(ValueTable source, String destinationTableName,
      Datasource destination) {
    return destination.createWriter(destinationTableName, source.getEntityType());
  }

  ValueTableWriter innerValueTableWriter(ValueTable source, String destinationTableName, Datasource destination) {
    return multiplexer == null
        ? createValueTableWriter(source, destinationTableName, destination)
        : new MultiplexingValueTableWriter(source, this, destination, multiplexer);
  }

  private void notifyListeners(Variable variable, boolean copied) {
    for(DatasourceCopyEventListener listener : listeners) {
      if(listener instanceof DatasourceCopyVariableEventListener) {
        DatasourceCopyVariableEventListener variableListener = (DatasourceCopyVariableEventListener) listener;
        if(copied) {
          variableListener.onVariableCopied(variable);
        } else {
          variableListener.onVariableCopy(variable);
        }
      }
    }
  }

  private void notifyListeners(ValueTable source, ValueSet valueSet, boolean copied, String... destination) {
    for(DatasourceCopyEventListener listener : listeners) {
      if(listener instanceof DatasourceCopyValueSetEventListener) {
        DatasourceCopyValueSetEventListener valueSetListener = (DatasourceCopyValueSetEventListener) listener;
        if(copied) {
          valueSetListener.onValueSetCopied(source, valueSet, destination);
        } else {
          valueSetListener.onValueSetCopy(source, valueSet);
        }
      }
    }
  }

  public void notifyListeners(ValueTable valueTable, String destinationTable, boolean copied) {
    for(DatasourceCopyEventListener listener : listeners) {
      if(listener instanceof DatasourceCopyValueTableEventListener) {
        DatasourceCopyValueTableEventListener valueTableListener = (DatasourceCopyValueTableEventListener) listener;
        if(copied) {
          valueTableListener.onValueTableCopied(valueTable, destinationTable);
        } else {
          valueTableListener.onValueTableCopy(valueTable, destinationTable);
        }
      }
    }
  }

  public interface DatasourceCopyEventListener {}

  public interface DatasourceCopyVariableEventListener extends DatasourceCopyEventListener {

    void onVariableCopy(Variable variable);

    void onVariableCopied(Variable variable);

  }

  public interface DatasourceCopyValueSetEventListener extends DatasourceCopyEventListener {

    void onValueSetCopy(ValueTable source, ValueSet valueSet);

    void onValueSetCopied(ValueTable source, ValueSet valueSet, String... tables);

  }

  public interface DatasourceCopyValueTableEventListener extends DatasourceCopyEventListener {

    void onValueTableCopy(ValueTable valueTable, String destinationTable);

    void onValueTableCopied(ValueTable valueTable, String destinationTable);

  }

  public interface VariableTransformer {
    Variable transform(Variable variable);
  }

  private static class NoOpTransformer implements VariableTransformer {
    @Override
    public Variable transform(Variable variable) {
      return variable;
    }
  }

  public interface MultiplexingStrategy {

    String multiplexVariable(Variable variable);

    String multiplexValueSet(VariableEntity entity, Variable variable);

  }

  private static class LoggingListener
      implements DatasourceCopyVariableEventListener, DatasourceCopyValueSetEventListener {

    @Override
    public void onValueSetCopied(ValueTable source, ValueSet valueSet, String... tables) {
      log.debug("Copied ValueSet for entity {}", valueSet.getVariableEntity());
    }

    @Override
    public void onVariableCopied(Variable variable) {
      log.debug("Copied variable {}", variable.getName());
    }

    @Override
    public void onValueSetCopy(ValueTable source, ValueSet valueSet) {
      log.debug("Copying ValueSet for entity {}", valueSet.getVariableEntity());
    }

    @Override
    public void onVariableCopy(Variable variable) {
      log.debug("Copying variable {}", variable.getName());
    }

  }

  private static class ThroughputListener implements DatasourceCopyValueSetEventListener {

    private static final NumberFormat TWO_DECIMAL_PLACES = DecimalFormat.getNumberInstance();

    static {
      TWO_DECIMAL_PLACES.setMaximumFractionDigits(2);
    }

    private long count = 0;

    private long allDuration = 0;

    private long start;

    @Override
    public void onValueSetCopy(ValueTable source, ValueSet valueSet) {
      start = System.currentTimeMillis();
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void onValueSetCopied(ValueTable source, ValueSet valueSet, String... tables) {
      long duration = System.currentTimeMillis() - start;
      allDuration += duration;
      count++;
      log.trace("ValueSet copied in {}s. Average copy duration for {} valueSets: {}s.",
          TWO_DECIMAL_PLACES.format(duration / 1000.0d), count,
          TWO_DECIMAL_PLACES.format(allDuration / (double) count / 1000.0d));
    }

  }

  public boolean isCopyValues() {
    return copyValues;
  }

  public void setCopyValues(boolean copyValues) {
    this.copyValues = copyValues;
  }

  public boolean isCopyNullValues() {
    return copyNullValues;
  }

  public boolean isCopyMetadata() {
    return copyMetadata;
  }

  public void setCopyMetadata(boolean copyMetadata) {
    this.copyMetadata = copyMetadata;
  }

}

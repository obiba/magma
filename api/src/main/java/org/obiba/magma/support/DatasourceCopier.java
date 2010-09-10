package org.obiba.magma.support;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.obiba.magma.Datasource;
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

import com.google.common.collect.ImmutableList;

public class DatasourceCopier {

  private static final Logger log = LoggerFactory.getLogger(DatasourceCopier.class);

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

    public Builder withListener(DatasourceCopyEventListener listener) {
      if(listener == null) throw new IllegalArgumentException("listener cannot be null");
      copier.listeners.add(listener);
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
  }

  private boolean copyNullValues = true;

  private boolean copyMetadata = true;

  private boolean copyValues = true;

  private List<DatasourceCopyEventListener> listeners = new LinkedList<DatasourceCopyEventListener>();

  private VariableTransformer variableTransformer = new NoOpTransformer();

  private MultiplexingStrategy multiplexer = null;

  private DatasourceCopier() {

  }

  private DatasourceCopier(DatasourceCopier other) {
    this.copyNullValues = other.copyNullValues;
    this.copyMetadata = other.copyMetadata;
    this.copyValues = other.copyValues;
    this.listeners = ImmutableList.copyOf(other.listeners);
    this.variableTransformer = other.variableTransformer;
    this.multiplexer = other.multiplexer;
  }

  public void copy(String source, String destination) throws IOException {
    copy(MagmaEngine.get().getDatasource(source), MagmaEngine.get().getDatasource(destination));
  }

  public void copy(Datasource source, Datasource destination) throws IOException {
    if(source == destination) {
      // Don't copy on itself! The caller probably didn't want to really do this, did they?
      log.warn("Invoked Datasource to Datasource copy with the same Datasource instance as source and destination. Nothing copied to or from Datasource '{}'.", source.getName());
      return;
    }

    log.info("Copying Datasource '{}' to '{}'.", source.getName(), destination.getName());
    for(ValueTable table : source.getValueTables()) {
      copy(table, destination);
    }
  }

  public void copy(ValueTable table, Datasource destination) throws IOException {
    copy(table, table.getName(), destination);
  }

  public void copy(ValueTable table, String tableName, Datasource destination) throws IOException {
    log.info("Copying ValueTable '{}' to '{}.{}' (copyMetadata={}, copyValues={}).", new Object[] { table.getName(), destination.getName(), tableName, copyMetadata, copyValues });

    ValueTableWriter vtw = innerValueTableWriter(table, tableName, destination);
    try {
      copy(table, tableName, vtw);
    } finally {
      closeValueTableWriter(vtw, table);
    }
  }

  public void copy(ValueTable table, String tableName, ValueTableWriter vtw) throws IOException {
    if(copyMetadata) {
      VariableWriter vw = vtw.writeVariables();
      try {
        copy(table, tableName, vw);
      } finally {
        vw.close();
      }
    }

    if(copyValues) {
      for(ValueSet valueSet : table.getValueSets()) {
        ValueSetWriter vsw = vtw.writeValueSet(valueSet.getVariableEntity());
        try {
          copy(table, valueSet, tableName, vsw);
        } finally {
          vsw.close();
        }
      }
    }
  }

  public void copy(ValueTable table, String tableName, VariableWriter vw) {
    if(copyMetadata) {
      for(Variable variable : table.getVariables()) {
        notifyListeners(variable, false);
        vw.writeVariable(variableTransformer.transform(variable));
        notifyListeners(variable, true);
      }
    }
  }

  public void copy(ValueTable table, ValueSet valueSet, String tableName, ValueSetWriter vsw) {
    if(copyValues) {
      notifyListeners(table, valueSet, false);
      for(Variable variable : table.getVariables()) {
        Value value = table.getValue(variable, valueSet);
        if(value.isNull() == false || copyNullValues) {
          vsw.writeValue(variableTransformer.transform(variable), value);
        }
      }

      if(vsw instanceof MultiplexedValueSetWriter) {
        Set<String> tables = ((MultiplexedValueSetWriter) vsw).getTables();
        notifyListeners(table, valueSet, true, tables.toArray(new String[] {}));
      } else {
        notifyListeners(table, valueSet, true, tableName);
      }
    }
  }

  public void copy(ValueTable source, String tableName, ValueSet valueSet, Variable[] variables, Value[] values, ValueSetWriter vsw) {
    if(copyValues) {
      notifyListeners(source, valueSet, false);
      for(int i = 0; i < variables.length; i++) {
        Value value = values[i];
        if(value.isNull() == false || copyNullValues) {
          Variable variable = variables[i];
          vsw.writeValue(variableTransformer.transform(variable), value);
        }
      }

      if(vsw instanceof MultiplexedValueSetWriter) {
        Set<String> tables = ((MultiplexedValueSetWriter) vsw).getTables();
        notifyListeners(source, valueSet, true, tables.toArray(new String[] {}));
      } else {
        notifyListeners(source, valueSet, true, tableName);
      }
    }
  }

  public ValueTableWriter createValueTableWriter(ValueTable source, String destinationTableName, Datasource destination) {
    return destination.createWriter(destinationTableName, source.getEntityType());
  }

  ValueTableWriter innerValueTableWriter(ValueTable source, String destinationTableName, Datasource destination) {
    if(multiplexer != null) {
      return new MultiplexingValueTableWriter(source, this, destination, multiplexer);
    } else {
      return createValueTableWriter(source, destinationTableName, destination);
    }
  }

  public void closeValueTableWriter(ValueTableWriter vtw, ValueTable table) throws IOException {
    vtw.close();
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

  public interface DatasourceCopyEventListener {
  }

  public interface DatasourceCopyVariableEventListener extends DatasourceCopyEventListener {

    public void onVariableCopy(Variable variable);

    public void onVariableCopied(Variable variable);

  }

  public interface DatasourceCopyValueSetEventListener extends DatasourceCopyEventListener {

    public void onValueSetCopy(ValueTable source, ValueSet valueSet);

    public void onValueSetCopied(ValueTable source, ValueSet valueSet, String... tables);

  }

  public interface DatasourceCopyValueTableEventListener extends DatasourceCopyEventListener {

    public void onValueTableCopy(ValueTable valueTable, String destinationTable);

    public void onValueTableCopied(ValueTable valueTable, String destinationTable);

  }

  public interface VariableTransformer {
    public Variable transform(Variable variable);
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

  private static class LoggingListener implements DatasourceCopyVariableEventListener, DatasourceCopyValueSetEventListener {

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

    private long count = 0;

    private long allDuration = 0;

    private long start;

    private NumberFormat twoDecimalPlaces = DecimalFormat.getNumberInstance();

    private ThroughputListener() {
      twoDecimalPlaces.setMaximumFractionDigits(2);
    }

    @Override
    public void onValueSetCopy(ValueTable source, ValueSet valueSet) {
      start = System.currentTimeMillis();
    }

    @Override
    public void onValueSetCopied(ValueTable source, ValueSet valueSet, String... tables) {
      long duration = System.currentTimeMillis() - start;
      allDuration += duration;
      count++;
      log.debug("ValueSet copied in {}s. Average copy duration for {} valueSets: {}s.", new Object[] { twoDecimalPlaces.format(duration / 1000.0d), count, twoDecimalPlaces.format(allDuration / (double) count / 1000.0d) });
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

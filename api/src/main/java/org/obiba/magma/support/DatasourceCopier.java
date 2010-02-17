package org.obiba.magma.support;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.audit.VariableEntityAuditLogManager;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourceCopier {

  private static final Logger log = LoggerFactory.getLogger(DatasourceCopier.class);

  public static class Builder {

    DatasourceCopier copier = new DatasourceCopier();

    public Builder() {
    }

    public static Builder newCopier() {
      return new Builder();
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

    public Builder withVariableEntityCopyEventListener(VariableEntityAuditLogManager auditLogManager, Datasource destination) {
      copier.listeners.add(new VariableEntityCopyEventListener(auditLogManager, destination));
      return this;
    }

    public DatasourceCopier build() {
      return copier;
    }
  }

  private boolean copyNullValues = true;

  private boolean copyMetadata = true;

  private boolean copyValues = true;

  private List<DatasourceCopyEventListener> listeners = new LinkedList<DatasourceCopyEventListener>();

  public DatasourceCopier() {

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
    log.info("Copying ValueTable '{}' to Datasource '{}' (copyMetadata={}, copyValues={}).", new Object[] { table.getName(), destination.getName(), copyMetadata, copyValues });
    // TODO: the target ValueTable name should probably be renamed to include the source Datasource's name
    notifyListeners(table, false);
    ValueTableWriter vtw = destination.createWriter(table.getName(), table.getEntityType());
    try {
      copy(table, vtw);
    } finally {
      vtw.close();
      notifyListeners(table, true);
    }
  }

  public void copy(ValueTable table, ValueTableWriter vtw) throws IOException {
    if(copyMetadata) {
      VariableWriter vw = vtw.writeVariables();
      try {
        copy(table, vw);
      } finally {
        vw.close();
      }
    }

    if(copyValues) {
      for(ValueSet valueSet : table.getValueSets()) {
        ValueSetWriter vsw = vtw.writeValueSet(valueSet.getVariableEntity());
        try {
          copy(table, valueSet, vsw);
        } finally {
          vsw.close();
        }
      }
    }
  }

  public void copy(ValueTable table, VariableWriter vw) {
    if(copyMetadata) {
      for(Variable variable : table.getVariables()) {
        notifyListeners(variable, false);
        vw.writeVariable(variable);
        notifyListeners(variable, true);
      }
    }
  }

  public void copy(ValueTable table, ValueSet valueSet, ValueSetWriter vsw) {
    if(copyValues) {
      notifyListeners(valueSet, false);
      for(Variable variable : table.getVariables()) {
        Value value = table.getValue(variable, valueSet);
        if(value.isNull() == false || copyNullValues) {
          vsw.writeValue(variable, value);
        }
      }
      notifyListeners(valueSet, true);
    }
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

  private void notifyListeners(ValueSet valueSet, boolean copied) {
    for(DatasourceCopyEventListener listener : listeners) {
      if(listener instanceof DatasourceCopyValueSetEventListener) {
        DatasourceCopyValueSetEventListener valueSetListener = (DatasourceCopyValueSetEventListener) listener;
        if(copied) {
          valueSetListener.onValueSetCopied(valueSet);
        } else {
          valueSetListener.onValueSetCopy(valueSet);
        }
      }
    }
  }

  private void notifyListeners(ValueTable valueTable, boolean copied) {
    for(DatasourceCopyEventListener listener : listeners) {
      if(listener instanceof DatasourceCopyValueTableEventListener) {
        DatasourceCopyValueTableEventListener valueTableListener = (DatasourceCopyValueTableEventListener) listener;
        if(copied) {
          valueTableListener.onValueTableCopied(valueTable);
        } else {
          valueTableListener.onValueTableCopy(valueTable);
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

    public void onValueSetCopy(ValueSet valueSet);

    public void onValueSetCopied(ValueSet valueSet);

  }

  public interface DatasourceCopyValueTableEventListener extends DatasourceCopyEventListener {

    public void onValueTableCopy(ValueTable valueTable);

    public void onValueTableCopied(ValueTable valueTable);

  }

  private static class LoggingListener implements DatasourceCopyVariableEventListener, DatasourceCopyValueSetEventListener {

    @Override
    public void onValueSetCopied(ValueSet valueSet) {
      log.debug("Copied ValueSet for entity {}", valueSet.getVariableEntity());
    }

    @Override
    public void onVariableCopied(Variable variable) {
      log.debug("Copied variable {}", variable.getName());
    }

    @Override
    public void onValueSetCopy(ValueSet valueSet) {
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
    public void onValueSetCopy(ValueSet valueSet) {
      start = System.currentTimeMillis();
    }

    @Override
    public void onValueSetCopied(ValueSet valueSet) {
      long duration = System.currentTimeMillis() - start;
      allDuration += duration;
      count++;
      log.debug("ValueSet copied in {}s. Average copy duration for {} valueSets: {}s.", new Object[] { twoDecimalPlaces.format(duration / 1000.0d), count, twoDecimalPlaces.format(allDuration / (double) count / 1000.0d) });
    }

  }

  private static class VariableEntityCopyEventListener implements DatasourceCopyValueSetEventListener, DatasourceCopyValueTableEventListener {

    private VariableEntityAuditLogManager auditLogManager;

    private Datasource source;

    private Datasource destination;

    public VariableEntityCopyEventListener(VariableEntityAuditLogManager auditLogManager, Datasource destination) {
      if(auditLogManager == null) throw new IllegalArgumentException("auditLogManager cannot be null");
      if(destination == null) throw new IllegalArgumentException("destination cannot be null");
      this.auditLogManager = auditLogManager;
      this.destination = destination;
    }

    @Override
    public void onValueSetCopied(ValueSet valueSet) {
      VariableEntity entity = valueSet.getVariableEntity();
      auditLogManager.createAuditEvent(auditLogManager.getAuditLog(entity), source, "COPY", createCopyDetails(entity));
    }

    @Override
    public void onValueSetCopy(ValueSet valueSet) {
    }

    @Override
    public void onValueTableCopied(ValueTable valueTable) {
    }

    @Override
    public void onValueTableCopy(ValueTable valueTable) {
      source = valueTable.getDatasource();
    }

    private Map<String, Value> createCopyDetails(VariableEntity entity) {
      Map<String, Value> details = new HashMap<String, Value>();
      details.put("destinationName", TextType.get().valueOf(destination.getName()));
      return details;
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

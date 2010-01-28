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

    public Builder withVariableEntityCopyEventListener(VariableEntityAuditLogManager auditLogManager, Datasource source, Datasource destination) {
      copier.listeners.add(new VariableEntityCopyEventListener(auditLogManager, source, destination));
      return this;
    }

    public DatasourceCopier build() {
      return copier;
    }
  }

  private boolean copyNullValues = true;

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
    log.info("Copying ValueTable '{}' to Datasource '{}'.", table.getName(), destination.getName());
    // TODO: the target ValueTable name should probably be renamed to include the source Datasource's name
    ValueTableWriter vtw = destination.createWriter(table.getName(), table.getEntityType());
    try {
      VariableWriter vw = vtw.writeVariables();
      try {
        for(Variable variable : table.getVariables()) {
          notifyListeners(variable, false);
          vw.writeVariable(variable);
          notifyListeners(variable, true);
        }
      } finally {
        vw.close();
      }
      for(ValueSet valueSet : table.getValueSets()) {
        notifyListeners(valueSet, false);
        ValueSetWriter vsw = vtw.writeValueSet(valueSet.getVariableEntity());
        try {
          for(Variable variable : table.getVariables()) {
            Value value = table.getValue(variable, valueSet);
            if(value.isNull() == false || copyNullValues) {
              vsw.writeValue(variable, value);
            }
          }
        } finally {
          vsw.close();
        }
        notifyListeners(valueSet, true);
      }
    } finally {
      vtw.close();
    }
  }

  private void notifyListeners(Variable variable, boolean copied) {
    for(DatasourceCopyEventListener listener : listeners) {
      if(copied) {
        listener.onVariableCopied(variable);
      } else {
        listener.onVariableCopy(variable);
      }
    }
  }

  private void notifyListeners(ValueSet valueSet, boolean copied) {
    for(DatasourceCopyEventListener listener : listeners) {
      if(copied) {
        listener.onValueSetCopied(valueSet);
      } else {
        listener.onValueSetCopy(valueSet);
      }
    }
  }

  public interface DatasourceCopyEventListener {

    public void onVariableCopy(Variable variable);

    public void onVariableCopied(Variable variable);

    public void onValueSetCopy(ValueSet valueSet);

    public void onValueSetCopied(ValueSet valueSet);

  }

  private static class LoggingListener implements DatasourceCopyEventListener {

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

  private static class ThroughputListener implements DatasourceCopyEventListener {

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

    @Override
    public void onVariableCopy(Variable variable) {
    }

    @Override
    public void onVariableCopied(Variable variable) {
    }

  }

  private static class VariableEntityCopyEventListener implements DatasourceCopyEventListener {

    private VariableEntityAuditLogManager auditLogManager;

    private Datasource source;

    private Datasource destination;

    public VariableEntityCopyEventListener(VariableEntityAuditLogManager auditLogManager, Datasource source, Datasource destination) {
      this.auditLogManager = auditLogManager;
      this.source = source;
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
    public void onVariableCopied(Variable variable) {
    }

    @Override
    public void onVariableCopy(Variable variable) {
    }

    private Map<String, Value> createCopyDetails(VariableEntity entity) {
      Map<String, Value> details = new HashMap<String, Value>();
      details.put("destinationName", TextType.get().valueOf(destination.getName()));
      return details;
    }

  }
}

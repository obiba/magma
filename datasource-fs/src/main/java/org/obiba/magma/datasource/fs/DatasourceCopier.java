package org.obiba.magma.datasource.fs;

import java.io.IOException;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
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

    public DatasourceCopier build() {
      return copier;
    }
  }

  private boolean copyNullValues = true;

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
    // TODO: the target ValueTable name should probably be renamed to include the source Datasource's name
    ValueTableWriter vtw = destination.createWriter(table.getName());
    try {
      VariableWriter vw = vtw.writeVariables();
      try {
        for(Variable variable : table.getVariables()) {
          vw.writeVariable(variable);
        }
      } finally {
        vw.close();
      }
      for(ValueSet valueSet : table.getValueSets()) {
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
      }
    } finally {
      vtw.close();
    }
  }
}

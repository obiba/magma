package org.obiba.magma.io;

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

public class DatasourceCopier {

  public DatasourceCopier() {

  }

  public void copy(String source, String destination) throws IOException {
    copy(MagmaEngine.get().getDatasource(source), MagmaEngine.get().getDatasource(destination));
  }

  public void copy(Datasource source, Datasource destination) throws IOException {
    for(ValueTable table : source.getValueTables()) {
      copy(table, destination);
    }
  }

  public void copy(ValueTable table, Datasource destination) throws IOException {
    ValueTableWriter vtw = destination.createWriter(table.getName());
    VariableWriter vw = vtw.writeVariables();
    for(Variable variable : table.getVariables()) {
      vw.writeVariable(variable);
    }
    vw.close();
    for(ValueSet valueSet : table.getValueSets()) {
      ValueSetWriter vsw = vtw.writeValueSet(valueSet.getVariableEntity());
      for(Variable variable : table.getVariables()) {
        Value value = table.getValue(variable, valueSet);
        vsw.writeValue(variable, value);
      }
      vsw.close();
    }
    vtw.close();
  }
}

package org.obiba.magma.js.views;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.Disposable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;

import com.google.common.collect.ImmutableSet;

/**
 * Get the list of variables from a value table in a datasource.
 */
public class DatasourceTableClause extends VariablesClause implements Disposable {

  private DatasourceFactory factory;

  private String table;

  private transient Datasource source;

  public void setTable(String table) {
    this.table = table;
  }

  public void setFactory(DatasourceFactory factory) {
    this.factory = factory;
  }

  @Override
  public void initialise() {
    ImmutableSet.Builder<Variable> variableSet = new ImmutableSet.Builder<Variable>();

    try {
      // read the variables from the excel datasource

      source = factory.create();
      Initialisables.initialise(source);

      if(source.hasValueTable(table)) {
        ValueTable vTable = source.getValueTable(table);
        for(Variable var : vTable.getVariables()) {
          variableSet.add(var);
        }
      }

      setVariables(variableSet.build());
      super.initialise();

    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }

  }

  @Override
  public void dispose() {
    Disposables.silentlyDispose(source);
  }

  @Override
  public VariableWriter createWriter() {
    source = factory.create();
    Initialisables.initialise(source);
    ValueTable vTable = source.getValueTable(table);
    return source.createWriter(table, vTable.getEntityType()).writeVariables();
  }

}

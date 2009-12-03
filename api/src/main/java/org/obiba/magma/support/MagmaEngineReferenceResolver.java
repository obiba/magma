package org.obiba.magma.support;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

public class MagmaEngineReferenceResolver {

  public MagmaEngineReferenceResolver() {

  }

  /**
   * Resolves a reference to a {@code VariableValueSource} using the specified {@code ValueTable} as a context and
   * {@code name}.
   * <p/>
   * The {@code name} attribute is expected to be of 3 forms:
   * <ul>
   * <li><code>SMOKER_STATUS</code> : will try to resolve the {@code VariableValueSource} named
   * <code>SMOKER_STATUS</code> within the provided {@code ValueTable}</li>
   * <li><code>otherTable:SMOKER_STATUS</code> : will try to resolve the {@code VariableValueSource} named
   * <code>SMOKER_STATUS</code> within a table named <code>otherTable</code> within the {@code Datasource} of the
   * provided {@code ValueTable}</li>
   * <li><code>ds.otherTable:SMOKER_STATUS</code> : will try to resolve the {@code VariableValueSource} named
   * <code>SMOKER_STATUS</code> within a table named <code>otherTable</code> within a {@code Datasource} named
   * <code>ds</code></li>
   * </ul>
   * @param context the context to use for resolving the variable name
   * @param name the name of the {@code VariableValueSource} to resolve
   * @return the resolved {@code VariableValueSource} instance
   * @throws NoSuchVariableException when the name cannot be resolved
   */
  public VariableValueSource resolve(ValueTable context, String name) throws NoSuchVariableException {
    if(name.indexOf(':') < 0) {
      // No, then lookup the source within the ValueSet's Collection
      return context.getVariableValueSource(name);
    } else {
      // Yes, then lookup the source within the engine.
      String parts[] = name.split(":");

      String tableReference = parts[0];
      String variableName = parts[1];

      if(tableReference.indexOf('.') < 0) {
        return context.getDatasource().getValueTable(tableReference).getVariableValueSource(variableName);
      } else {
        parts = tableReference.split("\\.");
        String datasourceName = parts[0];
        String tableName = parts[1];
        return MagmaEngine.get().getDatasource(datasourceName).getValueTable(tableName).getVariableValueSource(variableName);
      }
    }
  }

  /**
   * Resolves the {@code ValueSet} of an entity within another {@code ValueTable}.
   * 
   * @param context
   * @param tableReference
   * @param entity
   * @return
   */
  public ValueSet resolve(ValueTable context, String tableReference, VariableEntity entity) {
    if(tableReference.indexOf('.') < 0) {
      return context.getDatasource().getValueTable(tableReference).getValueSet(entity);
    } else {
      String[] parts = tableReference.split("\\.");
      String datasourceName = parts[0];
      String tableName = parts[1];
      return MagmaEngine.get().getDatasource(datasourceName).getValueTable(tableName).getValueSet(entity);
    }
  }
}

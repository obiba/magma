package org.obiba.magma.support;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;

public class MagmaEngineReferenceResolver {

  private String datasourceName;

  private String tableName;

  private String variableName;

  private MagmaEngineReferenceResolver() {
  }

  public ValueTable resolveTable(ValueSet context) throws NoSuchDatasourceException, NoSuchValueTableException {
    if(datasourceName == null) {
      return context.getValueTable();
    }
    return MagmaEngine.get().getDatasource(datasourceName).getValueTable(tableName);
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
   * @throws NoSuchDatasourceException when the referenced datasource cannot be resolved
   * @throws NoSuchValueTableException when the referenced value table cannot be resolved
   * @throws NoSuchVariableException when the variable cannot be resolved
   */
  public VariableValueSource resolveSource(ValueSet context) throws NoSuchDatasourceException, NoSuchValueTableException, NoSuchVariableException {
    return resolveTable(context).getVariableValueSource(variableName);
  }

  /**
   * Returns true if the specified {@code ValueSet} is within a different table than the referenced {@code ValueTable}
   * @param context
   * @return
   */
  public boolean isJoin(ValueSet context) {
    ValueTable table = resolveTable(context);
    return table != context.getValueTable();
  }

  public ValueSet join(ValueSet context) {
    ValueTable table = resolveTable(context);
    return table.getValueSet(context.getVariableEntity());
  }

  public static MagmaEngineReferenceResolver valueOf(String name) {
    MagmaEngineReferenceResolver reference = new MagmaEngineReferenceResolver();
    // Is this a fully qualified name?
    if(name.indexOf(':') < 0) {
      // No
      reference.variableName = name;
    } else {
      // Yes, then lookup the source within the engine.
      String parts[] = name.split(":");

      String tableReference = parts[0];
      reference.variableName = parts[1];

      if(tableReference.indexOf('.') < 0) {
        reference.tableName = tableReference;
      } else {
        parts = tableReference.split("\\.");
        reference.datasourceName = parts[0];
        reference.tableName = parts[1];
      }
    }
    return reference;
  }
}

package org.obiba.magma.support;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;

import com.google.common.annotations.VisibleForTesting;

public class MagmaEngineReferenceResolver {

  private String datasourceName;

  private String tableName;

  private String variableName;

  private MagmaEngineReferenceResolver() {
  }

  /**
   * Resolves a reference to a {@code ValueTable} using the specified {@code ValueSet} as a context.
   */
  public ValueTable resolveTable(ValueSet context) throws NoSuchDatasourceException, NoSuchValueTableException {
    if(tableName == null) {
      if(context == null) {
        throw new IllegalStateException("cannot resolve table without a context.");
      }
      return context.getValueTable();
    }

    Datasource ds = null;
    if(datasourceName == null) {
      if(context == null) {
        throw new IllegalStateException("cannot resolve datasource without a context.");
      }
      ds = context.getValueTable().getDatasource();
    } else {
      ds = MagmaEngine.get().getDatasource(datasourceName);
    }
    return ds.getValueTable(tableName);
  }

  /**
   * Resolves a reference to a {@code VariableValueSource} using the specified {@code ValueSet} as a context.
   */
  public VariableValueSource resolveSource(ValueSet context) throws NoSuchDatasourceException, NoSuchValueTableException, NoSuchVariableException {
    return resolveTable(context).getVariableValueSource(variableName);
  }

  /**
   * Resolves a reference to a {@code VariableValueSource} without a context. This can be used to resolve fully
   * qualified variable names.
   */
  public VariableValueSource resolveSource() throws NoSuchDatasourceException, NoSuchValueTableException, NoSuchVariableException {
    return MagmaEngine.get().getDatasource(datasourceName).getValueTable(tableName).getVariableValueSource(variableName);
  }

  /**
   * Resolves a reference to a {@link ValueTable} without a context. Used to resolve fully qualified variable names in
   * the forms {@code 'datasourceName.TableName'} and {@code 'datasourceName.TableName:VARIABLE_NAME'}.
   * @return
   * @throws NoSuchDatasourceException when the {@link Datasource} can not be found.
   * @throws NoSuchValueTableException when the {@link ValueTable} can not be found.
   */
  public ValueTable resolveTable() throws NoSuchDatasourceException, NoSuchValueTableException {
    return MagmaEngine.get().getDatasource(datasourceName).getValueTable(tableName);
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

  /**
   * The {@code name} attribute is expected to be of 4 forms:
   * <ul>
   * <li><code>SMOKER_STATUS</code> : will try to resolve the {@code VariableValueSource} named
   * <code>SMOKER_STATUS</code> within the provided {@code ValueTable}</li>
   * <li><code>otherTable:SMOKER_STATUS</code> : will try to resolve the {@code VariableValueSource} named
   * <code>SMOKER_STATUS</code> within a table named <code>otherTable</code> within the {@code Datasource} of the
   * provided {@code ValueTable}</li>
   * <li><code>ds.otherTable:SMOKER_STATUS</code> : will try to resolve the {@code VariableValueSource} named
   * <code>SMOKER_STATUS</code> within a table named <code>otherTable</code> within a {@code Datasource} named
   * <code>ds</code></li>
   * <li><code>ds.otherTable</code> : will try to resolve the {@code TableValue} named <code>otherTable</code> within a
   * {@code Datasource} named <code>ds</code></li>
   * </ul>
   * @param name the name of the {@code VariableValueSource} to resolve
   * @return an instance of {@code MagmaEngineReferenceResolver}
   * @throws NoSuchDatasourceException when the referenced datasource cannot be resolved
   * @throws NoSuchValueTableException when the referenced value table cannot be resolved
   * @throws NoSuchVariableException when the variable cannot be resolved
   */
  public static MagmaEngineReferenceResolver valueOf(String name) {
    MagmaEngineReferenceResolver reference = new MagmaEngineReferenceResolver();
    // Is this a fully qualified name?
    if(name.indexOf(':') < 0) {
      // No
      if(name.indexOf('.') < 0) {
        reference.variableName = name;
      } else {
        String parts[] = name.split("\\.");
        reference.datasourceName = parts[0];
        reference.tableName = parts[1];
      }
    } else {
      // Yes
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

  @VisibleForTesting
  String getDatasourceName() {
    return datasourceName;
  }

  @VisibleForTesting
  String getTableName() {
    return tableName;
  }

  @VisibleForTesting
  String getVariableName() {
    return variableName;
  }
}

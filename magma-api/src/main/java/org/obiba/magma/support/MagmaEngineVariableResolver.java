/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;

public class MagmaEngineVariableResolver extends MagmaEngineReferenceResolver {

  private MagmaEngineVariableResolver() {
  }

  /**
   * Resolves a reference to a {@code VariableValueSource} using the specified {@code ValueSet} as a context.
   */
  public VariableValueSource resolveSource(ValueSet context)
      throws NoSuchDatasourceException, NoSuchValueTableException, NoSuchVariableException {
    return resolveTable(context).getVariableValueSource(getVariableName());
  }

  /**
   * Resolves a reference to a {@code VariableValueSource} using the specified {@code ValueTable} as a context.
   */
  public VariableValueSource resolveSource(ValueTable context) throws NoSuchVariableException {
    return resolveTable(context).getVariableValueSource(getVariableName());
  }

  /**
   * Resolves a reference to a {@code VariableValueSource} without a context. This can be used to resolve fully
   * qualified variable names.
   */
  public VariableValueSource resolveSource()
      throws NoSuchDatasourceException, NoSuchValueTableException, NoSuchVariableException {
    return MagmaEngine.get().getDatasource(getDatasourceName()).getValueTable(getTableName())
        .getVariableValueSource(getVariableName());
  }

  /**
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
   *
   * @param name the name of the {@code VariableValueSource} to resolve
   * @return an instance of {@code MagmaEngineReferenceResolver}
   * @throws NoSuchDatasourceException when the referenced datasource cannot be resolved
   * @throws NoSuchValueTableException when the referenced value table cannot be resolved
   * @throws NoSuchVariableException when the variable cannot be resolved
   */
  public static MagmaEngineVariableResolver valueOf(String name) {
    MagmaEngineVariableResolver reference = new MagmaEngineVariableResolver();
    // Is this a fully qualified name?
    if(name.contains(":")) {
      String[] parts = name.split(":");
      String tableReference = parts[0];
      if(parts.length > 1) { // Handle 'datasourceName.ValueTableName:' case.
        reference.setVariableName(parts[1]);
      }

      if(tableReference.contains(".")) {
        String[] tableParts = tableReference.split("\\.");
        reference.setDatasourceName(tableParts[0]);
        reference.setTableName(tableParts[1]);
      } else {
        reference.setTableName(tableReference);
      }
    } else {
      reference.setVariableName(name);
    }
    return reference;
  }

}

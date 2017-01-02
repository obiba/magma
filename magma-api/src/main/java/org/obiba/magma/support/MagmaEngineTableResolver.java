/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;

public class MagmaEngineTableResolver extends MagmaEngineReferenceResolver {

  private MagmaEngineTableResolver() {
  }

  /**
   * Resolves a reference to a {@link ValueTable} without a context. Used to resolve fully qualified variable names in
   * the form {@code 'datasourceName.TableName'}.
   *
   * @return
   * @throws NoSuchDatasourceException when the {@link Datasource} can not be found.
   * @throws NoSuchValueTableException when the {@link ValueTable} can not be found.
   */
  public ValueTable resolveTable() throws NoSuchDatasourceException, NoSuchValueTableException {
    return MagmaEngine.get().getDatasource(getDatasourceName()).getValueTable(getTableName());
  }

  /**
   * The {@code name} attribute is expected to be of 2 forms:
   * <ul>
   * <li><code>ds.otherTable</code> : will try to resolve the {@code TableValue} named <code>otherTable</code> within a
   * {@code Datasource} named <code>ds</code>.</li>
   * <li><code>otherTable</code> : will try to resolve the {@code TableValue} named <code>otherTable</code> within a
   * table named <code>otherTable</code> within the {@code Datasource} of the provided {@code ValueTable}</li>
   * </ul>
   *
   * @param name the name of the {@code ValueTable} to resolve
   * @return an instance of {@code MagmaEngineTableResolver}
   */
  public static MagmaEngineTableResolver valueOf(String name) {
    MagmaEngineTableResolver resolver = new MagmaEngineTableResolver();
    if(name.contains(".")) {
      String[] parts = name.split("\\.");
      resolver.setDatasourceName(parts[0]);
      resolver.setTableName(parts[1]);
    } else {
      resolver.setTableName(name);
    }
    return resolver;
  }
}

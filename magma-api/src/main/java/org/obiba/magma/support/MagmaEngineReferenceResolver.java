/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import java.util.Objects;

import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;

/**
 * Contains common elements of all MagmaEngineReferenceResovler classes.
 */
public abstract class MagmaEngineReferenceResolver {

  private String datasourceName;

  private String tableName;

  private String variableName;

  /**
   * Resolves a reference to a {@code ValueTable} using the specified {@code ValueTable} as a context.
   */
  public ValueTable resolveTable(@Nullable ValueTable context)
      throws NoSuchDatasourceException, NoSuchValueTableException {
    if(tableName == null) {
      if(context == null) {
        throw new IllegalStateException("cannot resolve table without a context.");
      }
      return context;
    }

    Datasource ds = null;
    if(datasourceName == null) {
      if(context == null) {
        throw new IllegalStateException("cannot resolve datasource without a context.");
      }
      ds = context.getDatasource();
    } else {
      ds = MagmaEngine.get().getDatasource(datasourceName);
    }
    return ds.getValueTable(tableName);
  }

  /**
   * Resolves a reference to a {@code ValueTable} using the specified {@code ValueSet} as a context.
   */
  public ValueTable resolveTable(ValueSet context) throws NoSuchDatasourceException, NoSuchValueTableException {
    return resolveTable(context != null ? context.getValueTable() : null);
  }

  /**
   * Returns true if the specified {@code ValueSet} is within a different table than the referenced {@code ValueTable}
   *
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

  public String getDatasourceName() {
    return datasourceName;
  }

  public String getTableName() {
    return tableName;
  }

  public String getVariableName() {
    return variableName;
  }

  void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  void setTableName(String tableName) {
    this.tableName = tableName;
  }

  void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(datasourceName, tableName, variableName);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    MagmaEngineReferenceResolver other = (MagmaEngineReferenceResolver) obj;
    return Objects.equals(datasourceName, other.datasourceName) && //
        Objects.equals(tableName, other.tableName) && //
        Objects.equals(variableName, other.variableName);
  }
}

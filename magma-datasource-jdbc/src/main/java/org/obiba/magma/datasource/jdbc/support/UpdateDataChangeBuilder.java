/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 */
package org.obiba.magma.datasource.jdbc.support;

import java.sql.Timestamp;
import java.util.Date;

import liquibase.change.ColumnConfig;
import liquibase.change.core.UpdateDataChange;

public class UpdateDataChangeBuilder {

  private final UpdateDataChange updateDataChange = new UpdateDataChange();

  public static UpdateDataChangeBuilder newBuilder() {
    return new UpdateDataChangeBuilder();
  }

  public UpdateDataChangeBuilder tableName(String tableName) {
    updateDataChange.setTableName(tableName);
    return this;
  }

  public UpdateDataChangeBuilder where(String whereClause) {
    updateDataChange.setWhere(whereClause);
    return this;
  }

  public UpdateDataChangeBuilder withColumn(String columnName, String columnValue) {
    ColumnConfig column = getColumn(columnName);
    column.setValue(columnValue);

    updateDataChange.addColumn(column);

    return this;
  }

  public UpdateDataChangeBuilder withColumn(String columnName, Boolean columnValue) {
    ColumnConfig column = getColumn(columnName);
    column.setValueBoolean(columnValue);

    updateDataChange.addColumn(column);

    return this;
  }

  public UpdateDataChangeBuilder withColumn(String columnName, Date columnValue) {
    ColumnConfig column = getColumn(columnName);
    if(columnValue != null) {
      column.setValueDate(new Timestamp(columnValue.getTime()));
    }

    updateDataChange.addColumn(column);

    return this;
  }

  public UpdateDataChange build() {
    return updateDataChange;
  }

  private ColumnConfig getColumn(String columnName) {
    ColumnConfig column = new ColumnConfig();
    column.setName(columnName);
    return column;
  }
}
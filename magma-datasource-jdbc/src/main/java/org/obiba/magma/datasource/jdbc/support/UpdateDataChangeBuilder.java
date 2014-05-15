/**
 *
 */
package org.obiba.magma.datasource.jdbc.support;

import java.sql.Timestamp;
import java.util.Date;

import liquibase.change.ColumnConfig;
import liquibase.change.UpdateDataChange;

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
    updateDataChange.setWhereClause(whereClause);
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
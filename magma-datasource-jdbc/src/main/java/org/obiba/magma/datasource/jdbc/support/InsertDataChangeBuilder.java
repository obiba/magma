/**
 *
 */
package org.obiba.magma.datasource.jdbc.support;

import java.sql.Timestamp;
import java.util.Date;

import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;

public class InsertDataChangeBuilder {

  private final InsertDataChange insertDataChange = new InsertDataChange();

  public static InsertDataChangeBuilder newBuilder() {
    return new InsertDataChangeBuilder();
  }

  public InsertDataChangeBuilder tableName(String tableName) {
    insertDataChange.setTableName(tableName);
    return this;
  }

  public InsertDataChangeBuilder withColumn(String columnName, String columnValue) {
    ColumnConfig column = getColumn(columnName);
    column.setValue(columnValue);

    insertDataChange.addColumn(column);

    return this;
  }

  public InsertDataChangeBuilder withColumn(String columnName, Boolean columnValue) {
    ColumnConfig column = getColumn(columnName);
    column.setValueBoolean(columnValue);

    insertDataChange.addColumn(column);

    return this;
  }

  public InsertDataChangeBuilder withColumn(String columnName, Date columnValue) {
    ColumnConfig column = getColumn(columnName);
    column.setValueDate(new Timestamp(columnValue.getTime()));

    insertDataChange.addColumn(column);

    return this;
  }

  public InsertDataChange build() {
    return insertDataChange;
  }

  private ColumnConfig getColumn(String columnName) {
    ColumnConfig column = new ColumnConfig();
    column.setName(columnName);
    return column;
  }
}
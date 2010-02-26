/**
 * 
 */
package org.obiba.magma.datasource.jdbc.support;

import java.util.Date;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.InsertDataChange;

public class InsertDataChangeBuilder {

  private InsertDataChange insertDataChange = new InsertDataChange();

  public static InsertDataChangeBuilder newBuilder() {
    return new InsertDataChangeBuilder();
  }

  public InsertDataChangeBuilder tableName(String tableName) {
    insertDataChange.setTableName(tableName);
    return this;
  }

  public InsertDataChangeBuilder withColumn(String columnName, String columnValue, boolean nullable) {
    ColumnConfig column = getColumn(columnName);
    column.setValue(columnValue);
    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setNullable(nullable);
    column.setConstraints(constraints);

    insertDataChange.addColumn(column);

    return this;
  }

  public InsertDataChangeBuilder withColumn(String columnName, String columnValue) {
    return withColumn(columnName, columnValue, false);
  }

  public InsertDataChangeBuilder withColumn(String columnName, Boolean columnValue, boolean nullable) {
    ColumnConfig column = getColumn(columnName);
    column.setValueBoolean(columnValue);
    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setNullable(nullable);
    column.setConstraints(constraints);

    insertDataChange.addColumn(column);

    return this;
  }

  public InsertDataChangeBuilder withColumn(String columnName, Boolean columnValue) {
    return withColumn(columnName, columnValue, false);
  }

  public InsertDataChangeBuilder withColumn(String columnName, Date columnValue, boolean nullable) {
    ColumnConfig column = getColumn(columnName);
    column.setValueDate(new java.sql.Timestamp(columnValue.getTime()));
    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setNullable(nullable);
    column.setConstraints(constraints);

    insertDataChange.addColumn(column);

    return this;
  }

  public InsertDataChangeBuilder withColumn(String columnName, Date columnValue) {
    return withColumn(columnName, columnValue, false);
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
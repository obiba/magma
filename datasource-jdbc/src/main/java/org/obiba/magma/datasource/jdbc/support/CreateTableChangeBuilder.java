/**
 * 
 */
package org.obiba.magma.datasource.jdbc.support;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateTableChange;

public class CreateTableChangeBuilder {

  private CreateTableChange createTableChange = new CreateTableChange();

  public static CreateTableChangeBuilder newBuilder() {
    return new CreateTableChangeBuilder();
  }

  public CreateTableChangeBuilder tableName(String tableName) {
    createTableChange.setTableName(tableName);
    return this;
  }

  public CreateTableChangeBuilder withPrimaryKeyColumn(String columnName, String columnType) {
    ColumnConfig column = getColumn(columnName, columnType);
    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setPrimaryKey(true);
    column.setConstraints(constraints);

    createTableChange.addColumn(column);

    return this;
  }

  public CreateTableChangeBuilder withColumn(String columnName, String columnType) {
    ColumnConfig column = getColumn(columnName, columnType);
    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setNullable(false);
    column.setConstraints(constraints);

    createTableChange.addColumn(column);

    return this;
  }

  public CreateTableChangeBuilder withNullableColumn(String columnName, String columnType) {
    ColumnConfig column = getColumn(columnName, columnType);
    ConstraintsConfig constraints = new ConstraintsConfig();
    constraints.setNullable(true);
    column.setConstraints(constraints);

    createTableChange.addColumn(column);

    return this;
  }

  public CreateTableChange build() {
    return createTableChange;
  }

  private ColumnConfig getColumn(String columnName, String columnType) {
    ColumnConfig column = new ColumnConfig();
    column.setName(columnName);
    column.setType(columnType);
    return column;
  }
}
/**
 *
 */
package org.obiba.magma.datasource.jdbc.support;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateTableChange;

public class CreateTableChangeBuilder {

  private final CreateTableChange createTableChange = new CreateTableChange();

  private ColumnConfig lastColumn;

  public static CreateTableChangeBuilder newBuilder() {
    return new CreateTableChangeBuilder();
  }

  public CreateTableChangeBuilder tableName(String tableName) {
    createTableChange.setTableName(tableName);
    return this;
  }

  public CreateTableChangeBuilder withColumn(String columnName, String columnType) {
    lastColumn = getColumn(columnName, columnType);
    createTableChange.addColumn(lastColumn);
    nullable();
    return this;
  }

  public CreateTableChangeBuilder notNull() {
    getConstraints().setNullable(false);
    return this;
  }

  public CreateTableChangeBuilder nullable() {
    getConstraints().setNullable(true);
    return this;
  }

  public CreateTableChangeBuilder primaryKey() {
    getConstraints().setPrimaryKey(true);
    notNull();
    return this;
  }

  public CreateTableChange build() {
    return createTableChange;
  }

  private ConstraintsConfig getConstraints() {
    ConstraintsConfig constraints = lastColumn.getConstraints();
    if(constraints == null) {
      lastColumn.setConstraints(constraints = new ConstraintsConfig());
    }
    return constraints;
  }

  private ColumnConfig getColumn(String columnName, String columnType) {
    ColumnConfig column = new ColumnConfig();
    column.setName(columnName);
    column.setType(columnType);
    return column;
  }
}
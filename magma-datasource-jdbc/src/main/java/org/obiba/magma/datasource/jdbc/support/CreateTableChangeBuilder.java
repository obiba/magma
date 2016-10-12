/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
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

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;

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
    withColumn(columnName, columnType, null);
    return this;
  }

  public CreateTableChangeBuilder withColumn(String columnName, String columnType, String defaultValue) {
    lastColumn = getColumn(columnName, columnType, defaultValue);
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

  private ColumnConfig getColumn(String columnName, String columnType, String defaultValue) {
    ColumnConfig column = new ColumnConfig();
    column.setName(columnName);
    column.setType(columnType);

    if(defaultValue != null) {
      switch (columnType.toUpperCase()) {
        case "DATE":
        case "TIMESTAMP":
          column.setDefaultValueDate(defaultValue);
          break;
        default:
          column.setDefaultValue(defaultValue);
      }
    }

    return column;
  }
}
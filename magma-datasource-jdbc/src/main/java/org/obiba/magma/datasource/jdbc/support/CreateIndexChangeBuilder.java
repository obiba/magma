package org.obiba.magma.datasource.jdbc.support;

import liquibase.change.AddColumnConfig;
import liquibase.change.core.CreateIndexChange;
import liquibase.structure.core.Column;

public class CreateIndexChangeBuilder {

  private final CreateIndexChange createIndexChange = new CreateIndexChange();

  public static CreateIndexChangeBuilder newBuilder() {
    return new CreateIndexChangeBuilder();
  }

  public CreateIndexChange build() {
    return createIndexChange;
  }

  public CreateIndexChangeBuilder table(String tableName) {
    createIndexChange.setTableName(tableName);
    return this;
  }

  public CreateIndexChangeBuilder name(String name) {
    createIndexChange.setIndexName(name);
    return this;
  }

  public CreateIndexChangeBuilder withColumn(String columnName) {
    createIndexChange.addColumn(new AddColumnConfig(new Column(columnName)));
    return this;
  }
}

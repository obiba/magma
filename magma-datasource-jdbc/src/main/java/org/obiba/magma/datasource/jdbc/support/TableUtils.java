package org.obiba.magma.datasource.jdbc.support;

import liquibase.structure.core.Table;

public class TableUtils {
  public static Table newTable(String name) {
    return new Table(null, null, name);
  }
}

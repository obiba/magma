package org.obiba.magma.datasource.jdbc.support;

import liquibase.structure.core.Table;

public class TableUtils {

  public static Table newTable(String name) {
    return new Table(null, null, name);
  }

  public static String normalize(String name) {
    String normalized = name.replaceAll("[^0-9a-zA-Z\\$_]", "");
    return normalized.length() > 64 ? normalized.substring(0, 63) : normalized;
  }
}

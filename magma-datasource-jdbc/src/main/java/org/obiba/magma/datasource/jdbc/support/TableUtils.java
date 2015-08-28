package org.obiba.magma.datasource.jdbc.support;

import liquibase.structure.core.Table;

public class TableUtils {

  public static Table newTable(String name) {
    return new Table(null, null, name);
  }

  public static String normalize(String name) {
    return normalize(name, -1);
  }

  public static String normalize(String name, int limit) {
    String normalized = name.replaceAll("[^0-9a-zA-Z\\$_]", "");
    return limit > 0 && normalized.length() > limit ? normalized.substring(0, limit - 1) : normalized;
  }
}

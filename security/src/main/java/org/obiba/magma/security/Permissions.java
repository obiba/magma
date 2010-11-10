package org.obiba.magma.security;

public class Permissions {

  public static String READ = "read";

  public static String datasourcePermission(String action) {
    return "datasource:" + action;
  }

  public static String datasourcePermission(String action, String instance) {
    return "datasource:" + action + ":" + instance;
  }

  public static String readDatasource() {
    return datasourcePermission(READ);
  }

  public static String readDatasource(String name) {
    return datasourcePermission(READ, name);
  }
}

package org.obiba.magma.datasource.jdbc.support;

public class NameConverter {

  public static String toMagmaName(String sqlName) {
    return sqlName.replace(':', '_').replace('.', '_');
  }

  public static String toSqlName(String magmaName) {
    return magmaName.replace(' ', '_').replace('.', '_').replace('-', '_').replace('\'', '_').replace('"', '_').replace('/', '_').replace('\\', '_').replace('<', '_').replace('>', '_');
  }

  public static String toMagmaVariableName(String sqlColumnName) {
    return sqlColumnName;
  }
}

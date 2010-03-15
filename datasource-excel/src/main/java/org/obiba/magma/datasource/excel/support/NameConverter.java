package org.obiba.magma.datasource.excel.support;

public class NameConverter {

  public static String toExcelName(String magmaName) {
    return magmaName.replace('/', '-').replace('\\', '_').replace(':', '_').replace('*', '_').replace('?', '_');
  }

}

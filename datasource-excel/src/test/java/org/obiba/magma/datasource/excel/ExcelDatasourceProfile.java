package org.obiba.magma.datasource.excel;

import java.io.File;

import org.obiba.magma.MagmaEngine;

public class ExcelDatasourceProfile {
  public static void main(String[] args) {
    new MagmaEngine();
    long before = System.currentTimeMillis();
    ExcelDatasource datasource = new ExcelDatasource("big", new File("src/test/resources/org/obiba/magma/datasource/excel/big.xlsx"));
    datasource.initialise();
    long duration = System.currentTimeMillis() - before;
    System.out.println("duration=" + duration / 1000 + "s");
  }
}

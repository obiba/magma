package org.obiba.magma.datasource.excel;

import org.obiba.core.util.FileUtil;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaEngine;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ExcelDatasourceProfile {

  public static void main(String... args) {
    new MagmaEngine();
    long before = System.currentTimeMillis();
    Initialisable datasource = new ExcelDatasource("big",
        FileUtil.getFileFromResource("org/obiba/magma/datasource/excel/big.xlsx"));
    datasource.initialise();
    long duration = System.currentTimeMillis() - before;
    System.out.println("duration=" + duration / 1000 + "s");
  }

}

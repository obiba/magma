package org.obiba.magma.datasource.excel;

import java.io.File;
import java.util.Date;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

public class ExcelTimestamps implements Timestamps {

  private final File excelFile;

  public ExcelTimestamps(File excelFile) {
    this.excelFile = excelFile;
  }

  @Override
  public Value getCreated() {
    // Not currently possible to read a file creation timestamp. Coming in JDK 7 NIO.
    return null;
  }

  @Override
  public Value getLastUpdate() {
    if(excelFile.exists()) {
      return DateTimeType.get().valueOf(new Date(excelFile.lastModified()));
    } else {
      return null;
    }
  }

}

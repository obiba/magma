package org.obiba.magma.datasource.excel;

import java.io.File;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

public class ExcelTimestamps implements Timestamps {

  private final File excelFile;

  public ExcelTimestamps(File excelFile) {
    this.excelFile = excelFile;
  }

  @NotNull
  @Override
  public Value getCreated() {
    // Not currently possible to read a file creation timestamp. Coming in JDK 7 NIO.
    return DateTimeType.get().nullValue();
  }

  @NotNull
  @Override
  public Value getLastUpdate() {
    return excelFile != null && excelFile.exists()
        ? DateTimeType.get().valueOf(new Date(excelFile.lastModified()))
        : DateTimeType.get().nullValue();
  }

}

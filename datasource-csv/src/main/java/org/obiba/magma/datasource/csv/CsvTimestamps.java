package org.obiba.magma.datasource.csv;

import java.io.File;
import java.util.Date;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

public class CsvTimestamps implements Timestamps {

  private final File variableFile;

  private final File dataFile;

  public CsvTimestamps(File variableFile, File dataFile) {
    this.variableFile = variableFile;
    this.dataFile = dataFile;
  }

  @Override
  public Value getCreated() {
    // Not currently possible to read a file creation timestamp. Coming in JDK 7 NIO.
    return DateTimeType.get().nullValue();
  }

  @Override
  public Value getLastUpdate() {
    Date variablesFileLastUpdated = variableFile == null ? null : new Date(variableFile.lastModified());
    Date dataFileLastUpdated = dataFile == null ? null : new Date(dataFile.lastModified());

    if(variablesFileLastUpdated != null && dataFileLastUpdated != null) {
      return DateTimeType.get().valueOf(
          variablesFileLastUpdated.after(dataFileLastUpdated) ? variablesFileLastUpdated : dataFileLastUpdated);
    }
    if(variablesFileLastUpdated == null && dataFileLastUpdated == null) {
      return DateTimeType.get().nullValue();
    }
    if(variablesFileLastUpdated != null) {
      return DateTimeType.get().valueOf(variablesFileLastUpdated);
    }
    return DateTimeType.get().valueOf(dataFileLastUpdated);
  }
}

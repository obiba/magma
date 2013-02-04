package org.obiba.magma.datasource.fs;

import java.util.Date;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

import de.schlichtherle.io.File;

public class FsTimestamps implements Timestamps {

  private final File valueTableDirectory;

  public FsTimestamps(File valueTableDirectory) {
    this.valueTableDirectory = valueTableDirectory;
  }

  @Override
  public Value getCreated() {
    // Not currently possible to read a file creation timestamp. Coming in JDK 7 NIO.
    return DateTimeType.get().nullValue();
  }

  @Override
  public Value getLastUpdate() {
    return valueTableDirectory.exists()
        ? DateTimeType.get().valueOf(new Date(valueTableDirectory.lastModified()))
        : DateTimeType.get().nullValue();
  }
}

package org.obiba.magma.datasource.csv.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads the first CSV file found in a Zip file.
 */
public class ZippedFileInputStream extends InputStream {

  private final ZipFile zipFile;

  private InputStream wrapped;

  public ZippedFileInputStream(File file) throws IOException {
    zipFile = new ZipFile(file);
    ZipEntry entry;
    while (this.wrapped == null && zipFile.entries().hasMoreElements()) {
      entry = zipFile.entries().nextElement();
      String name = entry.getName();
      if (name.endsWith(".csv") || name.endsWith(".tsv")) {
        this.wrapped = zipFile.getInputStream(entry);
      }
    }
  }

  @Override
  public int read() throws IOException {
    return wrapped.read();
  }

  @Override
  public void close() throws IOException {
    wrapped.close();
    zipFile.close();
  }
}

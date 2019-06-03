package org.obiba.magma.datasource.csv.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZippedFileOutputStream extends OutputStream {

  private final ZipOutputStream wrapped;

  public ZippedFileOutputStream(File file) throws IOException {
    this.wrapped = new ZipOutputStream(new FileOutputStream(file));
    ZipEntry entry = new ZipEntry(file.getName().replace(".zip", ".csv"));
    wrapped.putNextEntry(entry);
  }

  @Override
  public void write(int i) throws IOException {
    wrapped.write(i);
  }

  @Override
  public void close() throws IOException {
    wrapped.closeEntry();
    wrapped.close();
  }
}

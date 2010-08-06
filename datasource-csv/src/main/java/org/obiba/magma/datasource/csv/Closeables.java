package org.obiba.magma.datasource.csv;

import java.io.Closeable;
import java.io.IOException;

final class Closeables {

  private Closeables() {
  };

  public static void closeQuietly(Closeable closable) {
    if(closable != null) {
      try {
        closable.close();
      } catch(IOException e) {
        // ignore
      }
    }
  }

}

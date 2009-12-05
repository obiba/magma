package org.obiba.magma.io;

import java.io.Closeable;
import java.io.IOException;

public final class Closeables {

  private Closeables() {
  };

  public static void closeQuietly(Closeable closable) {
    if(closable != null) {
      try {
        closable.close();
      } catch(IOException e) {
      }
    }
  }

}

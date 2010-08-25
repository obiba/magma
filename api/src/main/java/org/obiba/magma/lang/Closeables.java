package org.obiba.magma.lang;

import java.io.Closeable;
import java.io.IOException;

public final class Closeables {

  private Closeables() {
  }

  /**
   * Calls {@code Closeable#close} on a non-null {@code closable} and eats {@code IOException} exceptions (any {@code
   * RuntimeException} will still be thrown).
   * @param closable the {@code Closeable} instance to close
   */
  public static void closeQuietly(Closeable closable) {
    if(closable != null) {
      try {
        closable.close();
      } catch(IOException e) {
        // Ignored
      }
    }
  }

}

package org.obiba.magma.lang;

import java.io.Closeable;
import java.io.IOException;

import javax.annotation.Nullable;

public final class Closeables {

  private Closeables() {
  }

  /**
   * Calls {@code Closeable#close} on a non-null {@code Closable} and eats {@code IOException} exceptions (any {@code
   * RuntimeException} will still be thrown).
   *
   * @param closable the {@code Closeable} instance to close
   */
  public static void closeQuietly(@Nullable Closeable closable) {
    if(closable == null) return;
    try {
      closable.close();
    } catch(IOException e) {
      // Ignored
    }
  }

  /**
   * Close without exception the list of {@code Closable}.
   * @param closables
   */
  public static void closeQuietly(@Nullable Closeable... closables) {
    if(closables == null) return;

    for(Closeable closeable : closables) {
      closeQuietly(closeable);
    }
  }

}

package org.obiba.magma.support;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Disposable;
import org.obiba.magma.MagmaRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Disposables {

  private static final Logger log = LoggerFactory.getLogger(Disposables.class);

  private Disposables() {}

  public static void silentlyDispose(@Nullable Disposable disposable) {
    try {
      dispose(disposable);
    } catch(RuntimeException e) {
      log.warn("Ignoring exception during disposable.dispose().", e);
    }
  }

  public static void silentlyDispose(@NotNull Object... disposable) {
    try {
      dispose(disposable);
    } catch(RuntimeException e) {
      log.warn("Ignoring exception during disposable.dispose().", e);
    }
  }

  public static void dispose(@Nullable Disposable disposable) {
    try {
      if(disposable != null) {
        disposable.dispose();
      }
    } catch(MagmaRuntimeException e) {
      throw e;
    } catch(RuntimeException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  public static void dispose(@Nullable Object disposable) {
    if(disposable instanceof Disposable) {
      dispose((Disposable) disposable);
    }
  }

  public static void dispose(@NotNull Disposable... disposables) {
    for(Disposable o : disposables) {
      dispose(o);
    }
  }

  public static void dispose(@NotNull Object... disposables) {
    for(Object o : disposables) {
      dispose(o);
    }
  }

  public static void dispose(@NotNull Iterable<?> disposables) {
    for(Object o : disposables) {
      dispose(o);
    }
  }
}

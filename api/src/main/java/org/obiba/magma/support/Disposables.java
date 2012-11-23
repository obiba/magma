package org.obiba.magma.support;

import org.obiba.magma.Disposable;
import org.obiba.magma.MagmaRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Disposables {

  private static final Logger log = LoggerFactory.getLogger(Disposables.class);

  public static final void silentlyDispose(Disposable disposable) {
    try {
      dispose(disposable);
    } catch(RuntimeException e) {
      log.warn("Ignoring exception during diposable.dispose().", e);
    }
  }

  public static final void silentlyDispose(Object... disposable) {
    try {
      dispose(disposable);
    } catch(RuntimeException e) {
      log.warn("Ignoring exception during diposable.dispose().", e);
    }
  }

  public static final void dispose(Disposable disposable) {
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

  public static final void dispose(Object disposable) {
    if(disposable instanceof Disposable) {
      dispose((Disposable) disposable);
    }
  }

  public static final void dispose(Disposable... disposables) {
    for(Disposable o : disposables) {
      dispose(o);
    }
  }

  public static final void dispose(Object... disposables) {
    for(Object o : disposables) {
      dispose(o);
    }
  }

  public static final void dispose(Iterable<?> disposables) {
    for(Object o : disposables) {
      dispose(o);
    }
  }
}

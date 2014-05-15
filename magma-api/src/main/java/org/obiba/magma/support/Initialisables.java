package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;

public final class Initialisables {

  private Initialisables() {}

  public static void initialise(@NotNull Initialisable initialisable) {
    try {
      initialisable.initialise();
    } catch(MagmaRuntimeException e) {
      throw e;
    } catch(RuntimeException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  public static void initialise(Object initialisable) {
    if(initialisable instanceof Initialisable) {
      initialise((Initialisable) initialisable);
    }
  }

  public static void initialise(@NotNull Initialisable... initialisable) {
    for(Initialisable o : initialisable) {
      initialise(o);
    }
  }

  public static void initialise(@NotNull Object... initialisable) {
    for(Object o : initialisable) {
      initialise(o);
    }
  }

  public static void initialise(@NotNull Iterable<?> initialisables) {
    for(Object o : initialisables) {
      initialise(o);
    }
  }

}

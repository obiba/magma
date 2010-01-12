package org.obiba.magma.support;

import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;

public final class Initialisables {

  public static final void initialise(final Initialisable initialisable) {
    try {
      initialisable.initialise();
    } catch(MagmaRuntimeException e) {
      throw e;
    } catch(RuntimeException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  public static final void initialise(final Object initialisable) {
    if(initialisable instanceof Initialisable) {
      initialise((Initialisable) initialisable);
    }
  }

  public static final void initialise(final Initialisable... initialisable) {
    for(Initialisable o : initialisable) {
      initialise(o);
    }
  }

  public static final void initialise(final Object... initialisable) {
    for(Object o : initialisable) {
      initialise(o);
    }
  }

}

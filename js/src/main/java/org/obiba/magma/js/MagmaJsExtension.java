package org.obiba.magma.js;

import org.mozilla.javascript.ContextFactory;
import org.obiba.magma.Initialisable;

/**
 * A {@code MagmaEngine} extension for creating manipulating variables using javascript.
 */
public class MagmaJsExtension implements Initialisable {

  @Override
  public void initialise() {
    MagmaContextFactory factory = new MagmaContextFactory();

    // Set MagmaContextFactory as the global factory
    ContextFactory.initGlobal(factory);

    // Initialise the shared scope
    factory.initialise();
  }
}

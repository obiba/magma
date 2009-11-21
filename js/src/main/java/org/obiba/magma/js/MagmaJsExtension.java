package org.obiba.magma.js;

import org.mozilla.javascript.ContextFactory;
import org.obiba.magma.Initialisable;

/**
 * A {@code MagmaEngine} extension for creating manipulating variables using javascript.
 */
public class MagmaJsExtension implements Initialisable {

  @Override
  public void initialise() {
    // Set MagmaContextFactory as the global factory. We can only do this if it hasn't been done already.
    if(ContextFactory.hasExplicitGlobal() == false) {
      MagmaContextFactory factory = new MagmaContextFactory();
      ContextFactory.initGlobal(factory);

      // Initialise the shared scope
      factory.initialise();
    }

  }
}

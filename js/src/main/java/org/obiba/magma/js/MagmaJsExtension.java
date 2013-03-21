package org.obiba.magma.js;

import org.mozilla.javascript.ContextFactory;
import org.obiba.magma.MagmaEngineExtension;

/**
 * A {@code MagmaEngine} extension for creating derived variables using JavaScript.
 */
public class MagmaJsExtension implements MagmaEngineExtension {

  private transient MagmaContextFactory magmaContextFactory = new MagmaContextFactory();

  public void setMagmaContextFactory(MagmaContextFactory magmaContextFactory) {
    this.magmaContextFactory = magmaContextFactory;
  }

  @Override
  public String getName() {
    return "magma-js";
  }

  @Override
  public void initialise() {
    // Set MagmaContextFactory as the global factory. We can only do this if it hasn't been done already.
    if(!ContextFactory.hasExplicitGlobal()) {
      ContextFactory.initGlobal(magmaContextFactory);
      // Initialise the shared scope
      magmaContextFactory.initialise();
    }

  }
}

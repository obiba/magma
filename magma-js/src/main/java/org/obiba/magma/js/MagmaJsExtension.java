package org.obiba.magma.js;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obiba.magma.MagmaEngineExtension;

/**
 * A {@code MagmaEngine} extension for creating derived variables using JavaScript.
 */
public class MagmaJsExtension implements MagmaEngineExtension {

  private static final long serialVersionUID = 2071830136892020358L;

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
    this.magmaContextFactory.initialise();
  }
}

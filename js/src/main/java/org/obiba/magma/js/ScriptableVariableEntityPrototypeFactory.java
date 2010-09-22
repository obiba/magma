package org.obiba.magma.js;

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.js.methods.ScriptableVariableEntityMethods;

public class ScriptableVariableEntityPrototypeFactory extends AbstractPrototypeFactory {

  public ScriptableVariableEntityPrototypeFactory() {
    addMethodProvider(ScriptableVariableEntityMethods.class);
  }

  @Override
  protected Scriptable newPrototype() {
    return new ScriptableVariableEntity();
  }
}

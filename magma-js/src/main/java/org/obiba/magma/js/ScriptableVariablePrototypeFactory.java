package org.obiba.magma.js;

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.js.methods.ScriptableVariableMethods;

public class ScriptableVariablePrototypeFactory extends AbstractPrototypeFactory {

  public ScriptableVariablePrototypeFactory() {
    addMethodProvider(ScriptableVariableMethods.class);
  }

  @Override
  protected Scriptable newPrototype() {
    return new ScriptableVariable();
  }

}

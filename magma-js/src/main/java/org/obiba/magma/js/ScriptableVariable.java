package org.obiba.magma.js;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.Variable;

/**
 * A {@code Scriptable} implementation for {@code Variable} objects.
 */
public class ScriptableVariable extends ScriptableObject {

  private static final long serialVersionUID = -4342110775412157728L;

  private static final String VARIABLE_CLASS_NAME = "Variable";

  private final Variable variable;

  /**
   * No-arg ctor for building the prototype
   */
  public ScriptableVariable() {
    variable = null;
  }

  public ScriptableVariable(Scriptable scope, Variable variable) {
    super(scope, ScriptableObject.getClassPrototype(scope, VARIABLE_CLASS_NAME));
    if(variable == null) throw new IllegalArgumentException("variable cannot be null");
    this.variable = variable;
  }

  @Override
  public String getClassName() {
    return VARIABLE_CLASS_NAME;
  }

  @Override
  public String toString() {
    return variable.toString();
  }

  public Variable getVariable() {
    return variable;
  }
}

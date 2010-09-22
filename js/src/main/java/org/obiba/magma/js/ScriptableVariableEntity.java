package org.obiba.magma.js;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.VariableEntity;

public class ScriptableVariableEntity extends ScriptableObject {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final String VARIABLE_ENTITY_CLASS_NAME = "VariableEntity";

  private final VariableEntity variableEntity;

  //
  // Constructors
  //

  /**
   * No-arg ctor for building the prototype
   */
  public ScriptableVariableEntity() {
    this.variableEntity = null;
  }

  public ScriptableVariableEntity(Scriptable scope, VariableEntity variableEntity) {
    super(scope, ScriptableObject.getClassPrototype(scope, VARIABLE_ENTITY_CLASS_NAME));
    if(variableEntity == null) throw new IllegalArgumentException("variableEntity cannot be null");
    this.variableEntity = variableEntity;
  }

  //
  // ScriptableObject Methods
  //

  @Override
  public String getClassName() {
    return VARIABLE_ENTITY_CLASS_NAME;
  }

  @Override
  public String toString() {
    return variableEntity.toString();
  }

  //
  // Methods
  //

  public VariableEntity getVariableEntity() {
    return variableEntity;
  }
}

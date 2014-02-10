package org.obiba.magma.js;

public class CircularVariableDependencyRuntimeException extends MagmaJsEvaluationRuntimeException {

  private static final long serialVersionUID = 6224713591897743417L;

  private final String variableRef;

  public CircularVariableDependencyRuntimeException(String variableRef) {
    super("Circular dependency for variable '" + variableRef + "'");
    this.variableRef = variableRef;
  }

  public String getVariableRef() {
    return variableRef;
  }
}

package org.obiba.magma.js;

public class CircularVariableDependencyRuntimeException extends MagmaJsEvaluationRuntimeException {

  private static final long serialVersionUID = 6224713591897743417L;

  private final String variableRef;

  public CircularVariableDependencyRuntimeException(JavascriptVariableValueSource.ReferenceNode node) {
    super("Circular dependency for variable '" + node.getVariableRef() + "'");
    variableRef = node.getVariableRef();
  }

  public String getVariableRef() {
    return variableRef;
  }
}

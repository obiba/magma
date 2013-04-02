package org.obiba.magma;

public class IncompatibleEntityTypeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private final String viewEntityType;

  private final String variableEntityType;

  public IncompatibleEntityTypeException(String viewEntityType, String variableEntityType) {
    super("Incompatible entity types: '" + viewEntityType + "' / '" + variableEntityType + "'");
    this.viewEntityType = viewEntityType;
    this.variableEntityType = variableEntityType;
  }

  public String getViewEntityType() {
    return viewEntityType;
  }

  public String getVariableEntityType() {
    return variableEntityType;
  }
}

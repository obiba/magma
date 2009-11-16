package org.obiba.magma;

public class NoSuchValueSetException extends MetaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  public NoSuchValueSetException(VariableEntity entity) {
    super("Value set does not exist for entity '" + entity + "'");
  }

  public NoSuchValueSetException(VariableEntity entity, String message) {
    super("Value set does not exist for entity '" + entity + "'. " + message);
  }
}

package org.obiba.magma;

public class NoSuchValueSetException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  public NoSuchValueSetException(ValueTable table, VariableEntity entity) {
    super("No ValueSet in table '" + table.getName() + "' for entity '" + entity + "'");
  }
}

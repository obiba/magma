package org.obiba.magma;

public class NoSuchValueTableException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  public NoSuchValueTableException(String tableName) {
    super("No value table exists with the specified name '" + tableName + "'");
  }

}

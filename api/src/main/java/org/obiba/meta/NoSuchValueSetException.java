package org.obiba.meta;

public class NoSuchValueSetException extends RuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  public NoSuchValueSetException(ValueSetReference reference) {
    super("Value set reference cannot be resolved '" + reference + "'");
  }

}

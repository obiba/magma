package org.obiba.magma;

public class VectorSourceNotSupportedException extends MagmaRuntimeException {

  private static final long serialVersionUID = -2930317701287038250L;

  public VectorSourceNotSupportedException(Class<? extends ValueSource> clazz) {
    super(clazz + " does not support VectorSource");
  }

}

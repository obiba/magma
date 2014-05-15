package org.obiba.magma;

public class VectorSourceNotSupportedException extends MagmaRuntimeException {

  private static final long serialVersionUID = -2930317701287038250L;

  private final Class<? extends ValueSource> valueSourceClass;

  public VectorSourceNotSupportedException(Class<? extends ValueSource> valueSourceClass) {
    super(valueSourceClass + " does not support VectorSource");
    this.valueSourceClass = valueSourceClass;
  }

  public Class<? extends ValueSource> getValueSourceClass() {
    return valueSourceClass;
  }
}

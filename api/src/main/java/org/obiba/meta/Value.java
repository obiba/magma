package org.obiba.meta;

import java.io.Serializable;

public class Value implements Serializable {

  private static final long serialVersionUID = 779426587031645153L;

  private ValueType type;

  private Serializable value;

  Value(ValueType type, Serializable value) {
    this.type = type;
    this.value = value;
  }

  public ValueType getValueType() {
    return type;
  }

  public Serializable getValue() {
    return value;
  }

  public boolean isNull() {
    return value == null;
  }

}

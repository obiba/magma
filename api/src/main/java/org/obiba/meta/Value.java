package org.obiba.meta;

import java.io.Serializable;

public class Value implements Serializable {

  private static final long serialVersionUID = 779426587031645153L;

  private ValueType type;

  private Serializable value;

  Value(ValueType type, Serializable value) {
    if(type == null) throw new IllegalArgumentException("type cannot be null");
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

  @Override
  public String toString() {
    return getValueType().toString(this);
  }

}

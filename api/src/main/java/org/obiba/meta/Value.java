package org.obiba.meta;

import java.io.Serializable;

public class Value implements Serializable {

  private static final long serialVersionUID = 779426587031645153L;

  private ValueType valueType;

  private Serializable value;

  Value(ValueType valueType, Serializable value) {
    if(valueType == null) throw new IllegalArgumentException("type cannot be null");
    this.valueType = valueType;
    this.value = value;
  }

  public ValueType getValueType() {
    return valueType;
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

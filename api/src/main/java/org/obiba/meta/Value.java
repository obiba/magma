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

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Value) {
      Value rhs = (Value) obj;
      return valueType == rhs.valueType && (isNull() ? rhs.isNull() : value.equals(rhs.value));
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    int hashcode = 37;
    hashcode *= hashcode + valueType.hashCode();
    hashcode *= hashcode + (isNull() ? 0 : value.hashCode());
    return hashcode;
  }
}

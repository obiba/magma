package org.obiba.meta;

import java.io.Serializable;

public class Value implements Serializable {

  private static final long serialVersionUID = 779426587031645153L;

  private static final Serializable NULL = "org.obiba.meta.Value.NULL".intern();

  private ValueType valueType;

  private Serializable value;

  Value(ValueType valueType, Serializable value) {
    if(valueType == null) throw new IllegalArgumentException("valueType cannot be null");
    if(value == null) {
      value = NULL;
    }
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
    return value == NULL;
  }

  @Override
  public String toString() {
    if(isNull()) {
      return value.toString();
    }
    return getValueType().toString(this);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(getClass() != obj.getClass()) {
      return false;
    }

    Value other = (Value) obj;

    return value.equals(other.value) && valueType.equals(other.valueType);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + value.hashCode();
    result = prime * result + valueType.hashCode();
    return result;
  }
}

package org.obiba.magma;

import java.io.Serializable;

public class Value implements Serializable {

  private static final long serialVersionUID = 779426587031645153L;

  private static final Serializable NULL = "org.obiba.magma.Value.NULL".intern();

  private ValueType valueType;

  private Object value;

  Value(ValueType valueType, Object value) {
    if(valueType == null) throw new IllegalArgumentException("valueType cannot be null");
    if(value == null) {
      value = NULL;
    }
    this.valueType = valueType;
    this.value = value;
  }

  public Value copy() {
    return valueType.valueOf(value);
  }

  public ValueType getValueType() {
    return valueType;
  }

  public Object getValue() {
    return value;
  }

  public boolean isNull() {
    return value == NULL;
  }

  /**
   * Returns true when this {@code Value} instance holds a sequence of other {@code Value} instances. In this situation,
   * the {@code ValueType} of this {@code Value} is the same as the {@code ValueType} of the items in the sequence. That
   * is if the sequence holds {@code Value} instances of type {@code TextType}, then this {@code Value} also has {@code
   * TextType} as its {@code ValueType}.
   * 
   * @return true when this {@code Value} holds a sequence of other {@code Value} instances
   */
  public boolean isSequence() {
    return false;
  }

  /**
   * Returns a {@code ValueSequence} view of this {@code Value} when {@code #isSequence()} returns true.
   * @return
   */
  public ValueSequence asSequence() {
    throw new IllegalStateException("value is not a sequence");
  }

  @Override
  public String toString() {
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

package org.obiba.magma;

import java.io.Serializable;

import javax.annotation.Nullable;

public class Value implements Serializable, Comparable<Value> {

  private static final long serialVersionUID = 779426587031645153L;

  private static final Serializable NULL = "org.obiba.magma.Value.NULL".intern();

  private final ValueType valueType;

  private final ValueLoader valueLoader;

  private transient int hashCode;

  Value(@Nullable ValueType valueType, Serializable value) {
    this(valueType, new StaticValueLoader(value));
  }

  Value(@Nullable ValueType valueType, ValueLoader valueLoader) {
    if(valueType == null) throw new IllegalArgumentException("valueType cannot be null");
    this.valueType = valueType;
    this.valueLoader = valueLoader == null ? new StaticValueLoader(null) : valueLoader;
  }

  public Value copy() {
    return valueType.valueOf(valueLoader.getValue());
  }

  public ValueType getValueType() {
    return valueType;
  }

  public Object getValue() {
    return isNull() ? null : valueLoader.getValue();
  }

  public boolean isNull() {
    return valueLoader.isNull();
  }

  /**
   * Returns true when this {@code Value} instance holds a sequence of other {@code Value} instances. In this situation,
   * the {@code ValueType} of this {@code Value} is the same as the {@code ValueType} of the items in the sequence. That
   * is if the sequence holds {@code Value} instances of type {@code TextType}, then this {@code Value} also has
   * {@code TextType} as its {@code ValueType}.
   * @return true when this {@code Value} holds a sequence of other {@code Value} instances
   */
  public boolean isSequence() {
    return false;
  }

  /**
   * Returns a {@code ValueSequence} view of this {@code Value} when {@code #isSequence()} returns true.
   * @return
   */
  @SuppressWarnings("ClassReferencesSubclass")
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
    // Shortcut
    Object val = valueLoader.getValue();
    Object otherVal = other.valueLoader.getValue();
    //noinspection SimplifiableIfStatement
    if(val == otherVal) {
      return true;
    }
    return val.equals(otherVal) && valueType.equals(other.valueType);
  }

  @Override
  public int hashCode() {
    if(hashCode == 0) {
      int prime = 31;
      int result = 1;
      result = prime * result + valueLoader.getValue().hashCode();
      result = prime * result + valueType.hashCode();
      hashCode = result;
    }
    return hashCode;
  }

  @Override
  public int compareTo(Value o) {
    return valueType.compare(this, o);
  }

  public static class StaticValueLoader implements ValueLoader {

    private static final long serialVersionUID = 8195664792459648506L;

    private final Serializable value;

    public StaticValueLoader(Serializable value) {
      this.value = value == null ? NULL : value;
    }

    @Override
    public boolean isNull() {
      return value == NULL;
    }

    @Override
    public Object getValue() {
      return value;
    }

  }
}

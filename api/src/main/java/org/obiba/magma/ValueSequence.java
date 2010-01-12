package org.obiba.magma;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * A {@code Value} instance that holds a sequence of other {@code Value} instances (its elements). The {@code ValueType}
 * of the sequence is the same as its elements. That is, if the elements have a value type {@code BooleanType}, then
 * this {@code ValueSequence} instance also has {@code BooleanType} as its value type.
 * 
 */
public class ValueSequence extends Value {

  private static final long serialVersionUID = -1965362009370797808L;

  ValueSequence(ValueType valueType, Iterable<Value> values) {
    super(valueType, values);
  }

  @Override
  public boolean isSequence() {
    return true;
  }

  @Override
  public ValueSequence asSequence() {
    return this;
  }

  @Override
  public Value copy() {
    return getValueType().sequenceOf(getValue());
  }

  @SuppressWarnings("unchecked")
  public Iterable<Value> getValue() {
    return (Iterable<Value>) super.getValue();
  }

  /**
   * The size of this sequence
   * @return
   */
  public int getSize() {
    return getValues().size();
  }

  /**
   * Returns an ordered view of the values.
   * @return
   */
  public List<Value> getValues() {
    return ImmutableList.copyOf(getValue());
  }

  /**
   * Returns the {@code i}th element of the sequence
   * @param i
   * @return
   * @throws IndexOutOfBoundsException when {@code i >=} {@code #getSize()}
   */
  public Value get(int i) {
    return getValues().get(i);
  }

  /**
   * Returns true if this sequence contains the specified {@code Value}
   * @param value
   * @return
   */
  public boolean contains(Value value) {
    return getValues().contains(value);
  }

  @Override
  public String toString() {
    if(isNull()) {
      return super.toString();
    }
    final StringBuilder sb = new StringBuilder();
    for(Value value : getValue()) {
      sb.append(value.toString()).append(',');
    }
    if(sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

}

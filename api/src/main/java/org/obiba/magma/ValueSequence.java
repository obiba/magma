package org.obiba.magma;

import java.util.List;

import com.google.common.collect.ImmutableList;

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

  @SuppressWarnings("unchecked")
  public Iterable<Value> getValue() {
    return (Iterable<Value>) super.getValue();
  }

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

  public Value get(int i) {
    return getValues().get(i);
  }

  @Override
  public String toString() {
    // Return [e1,e2,e3...,en]
    // If we want to parse the string back as a ValueSequence, then we'll need to escape certain characters for TextType
    throw new UnsupportedOperationException();
  }

}

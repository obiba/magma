package org.obiba.magma;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.LocaleType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

/**
 * A {@code Value} instance that holds a sequence of other {@code Value} instances (its elements). The {@code ValueType}
 * of the sequence is the same as its elements. That is, if the elements have a value type {@code BooleanType}, then
 * this {@code ValueSequence} instance also has {@code BooleanType} as its value type.
 */
public class ValueSequence extends Value {

  private static final long serialVersionUID = -1965362009370797808L;

  ValueSequence(@Nonnull ValueType valueType, @Nullable Iterable<Value> values) {
    super(valueType, (Serializable) values);
  }

  @Override
  public boolean isSequence() {
    return true;
  }

  @Nonnull
  @Override
  public ValueSequence asSequence() {
    return this;
  }

  @Nonnull
  @Override
  public Value copy() {
    return getValueType().sequenceOf(getValue());
  }

  @Nullable
  @Override
  @SuppressWarnings("unchecked")
  public Iterable<Value> getValue() {
    return (Iterable<Value>) super.getValue();
  }

  /**
   * Returns a copy of this {@link ValueSequence} with the {@link Value}s sorted in the natural order provided that the
   * {@code ValueSequence} was constructed with an {@link Iterable} value that is also of the type {@link List}. If the
   * {@code Iterable} value is not of type {@code List} then this method will have no effect. Note that some
   * {@link ValueType}s such as {@link BinaryType} and {@link LocaleType} do not have a natural sort order and
   * {@code ValueSequence}s of those types will not be modified by this method.
   */
  public ValueSequence sort() {
    return getValueType().sequenceOf(Ordering.natural().immutableSortedCopy(getValue()));
  }

  /**
   * Returns a copy of this {@link ValueSequence} with the {@link Value}s sorted based on the specific
   * {@link Comparator} implementation.
   *
   * @param comparator Custom Comparator which will be used to sort the ValueSequence.
   */
  public ValueSequence sort(Comparator<Value> comparator) {
    return getValueType().sequenceOf(Ordering.from(comparator).immutableSortedCopy(getValue()));
  }

  /**
   * The length of this sequence
   *
   * @return
   */
  public int getSize() {
    return getValues().size();
  }

  /**
   * The size is the sum of the size of the values in the sequence.
   *
   * @return
   */
  @Override
  public long getLength() {
    long size = 0;
    for(Value val : getValues()) {
      size += val.getLength();
    }
    return size;
  }

  /**
   * Returns an ordered view of the values.
   *
   * @return
   */
  @Nonnull
  public List<Value> getValues() {
    Iterable<Value> value = getValue();
    return value == null ? ImmutableList.<Value>of() : ImmutableList.copyOf(value);
  }

  /**
   * Returns the {@code i}th element of the sequence
   *
   * @param i
   * @return
   * @throws IndexOutOfBoundsException when {@code i >=} {@code #getSize()}
   */
  public Value get(int i) {
    return getValues().get(i);
  }

  /**
   * Returns true if this sequence contains the specified {@code Value}
   *
   * @param value
   * @return
   */
  public boolean contains(@Nonnull Value value) {
    List<Value> values = getValues();
    return values.contains(value);
  }
}

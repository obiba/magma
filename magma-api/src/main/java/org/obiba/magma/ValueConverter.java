package org.obiba.magma;

/**
 * Contract for converting {@code Value} instances from one {@code ValueType} to another.
 * <p/>
 * The {@code converts} methods will be invoked to determine if this {@code ValueConverter} can handle the conversion
 * from one type to another. If so, its {@code convert()} method is invoked. Implementations should not convert
 * {@code ValueSequence} instances. These are handled externally, and each value in a sequence will be converted using
 * the {@code convert()} method.
 */
public interface ValueConverter {

  /**
   * Returns true when this instance can convert from value instances of type {@code from} to value instances of type
   * {@code to}
   *
   * @param from the {@code ValueType} the original {@code Value} instance has
   * @param to the {@code ValueType} the resulting {@code Value} instance has
   * @return true when this converter can handle the conversion
   */
  boolean converts(ValueType from, ValueType to);

  /**
   * Converts the given {@code Value} instance to the {@code ValueType} {@code to}
   *
   * @param value the {@code Value} instance to convert.
   * @param to the {@code ValueType} to convert to
   * @return a {@code Value} instance of {@code ValueType} {@code to}
   */
  Value convert(Value value, ValueType to);

}

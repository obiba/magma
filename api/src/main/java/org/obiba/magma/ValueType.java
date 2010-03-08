package org.obiba.magma;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Provides a common interface for all types of values available in the {@code MagmaEngine}. Through this interface,
 * callers may obtain information on the type's nature, obtain {@code Value} instances from Java objects and convert
 * values to a stable string representation (and back).
 */
public interface ValueType extends Serializable, Comparator<Value> {

  /**
   * Provides access to all {@code ValueType} instances by name or Java type. Also allows creating {@code Value}
   * instances from Java objects.
   */
  public static final class Factory {

    /**
     * Returns the {@code ValueType} instance for the specified {@code name}.
     * @param name the unique name of the {@code ValueType} instance
     * @return the {@code ValueType} instance for the specified {@code name}
     * @throws IllegalArgumentException when no type exists for the specified name
     */
    public static ValueType forName(String name) throws IllegalArgumentException {
      return MagmaEngine.get().getValueTypeFactory().forName(name);
    }

    /**
     * Returns the {@code ValueType} instance that accepts the specified java {@code Class}, as specified by the
     * {@link ValueType#acceptsJavaClass(Class)} method.
     * 
     * @param javaClass the Java {@code Class} to test
     * @return the {@code ValueType} instance that accepts the specified {@code Class}.
     * @throws IllegalArgumentException when no type exists for the specified class
     */
    public static ValueType forClass(Class<?> javaClass) throws IllegalArgumentException {
      return MagmaEngine.get().getValueTypeFactory().forClass(javaClass);
    }

    /**
     * Returns a new {@code Value} instance for the specified object instance. This method will determine the proper
     * {@code ValueType} by passing the object's class to the {@link #forClass(Class)} method. Note that this method
     * does not accept {@code null} as it would be impossible to determine the {@code ValueType} of the returned {@code
     * Value}.
     * @param value the object instance for which to obtain a {@code Value} instance. Cannot be null.
     * @return a {@code Value} instance for the specified object.
     * @throws IllegalArgumentException when {@code value} is null.
     */
    public static Value newValue(Object value) {
      if(value == null) {
        throw new IllegalArgumentException("cannot determine ValueType for null object");
      }
      return newValue(forClass(value.getClass()), value);
    }

    public static Value newValue(ValueType type, Object value) {
      return new Value(type, value);
    }

    public static ValueSequence newSequence(ValueType type, Iterable<Value> values) {
      return new ValueSequence(type, values);
    }

  }

  /**
   * The unique name of this {@code ValueType}.
   * @return this type's unique name
   */
  public String getName();

  /**
   * The java class of the value held in a {@code Value} instance of this {@code ValueType}. That is, when a {@code
   * Value} instance if of this {@code ValueType}, the contained object should be of the type returned by this method.
   * 
   * @return the normalized java class for this {@code ValueType}
   */
  public Class<?> getJavaClass();

  /**
   * Returns true if an instance of the specified class is suitable for invoking the {@link #valueOf(Object)} method.
   * 
   * @param clazz the type to check
   * @return true if the {@link #valueOf(Object)} can be called with an instance of the specified class.
   */
  public boolean acceptsJavaClass(Class<?> clazz);

  /**
   * Returns true if this type represents a date, time or both.
   * @return if this type represents a date, time or both.
   */
  public boolean isDateTime();

  /**
   * Returns true if this type represents a number.
   * @return true if this type represents a number
   */
  public boolean isNumeric();

  /**
   * Returns a {@code Value} instance that represents the null value for this type. Calling {@link Value#isNull()} on
   * the returned instance will return true.
   * @return a {@code Value} instance for null.
   */
  public Value nullValue();

  /**
   * Returns a {@code ValueSequence} instance that represents the null value for this type. Calling
   * {@link Value#isNull()} on the returned instance will return true.
   * @return a {@code ValueSequence} instance for null
   */
  public ValueSequence nullSequence();

  /**
   * Returns a string representation of the {@code value}. The string returned can be passed to the
   * {@link #valueOf(String)} method which should return an equivalent {@code Value} instance.
   * 
   * @param value the value to convert to a string
   * @return a {@code String} representation of the {@code value}.
   */
  public String toString(Value value);

  /**
   * Converts a string representation of a {@code value} to a {@code Value} instance. The string representation should
   * match the expected format which is specified by the {@link #toString(Value)} method.
   * 
   * @param string a string representation of the value. May be null, in which case, the returned value is that of
   * calling {@link #nullValue()}
   * @return a {@code Value} instance after converting its string representation.
   */
  public Value valueOf(String string);

  /**
   * Builds a {@code Value} instance after converting the specified object to the normalized type returned by
   * {@link #getJavaClass()} method. Note that this method accepts null values and should return a non-null {@code
   * Value} instance.
   * <p/>
   * For example, this method would convert instances of {@code java.util.Date}, {@code java.sql.Date}, {@code
   * java.sql.Timestamp}, {@code java.util.Calendar} to an instance of {@code java.util.Date} and return a {@code Value}
   * instance containing the normalized instance.
   * 
   * @param object the instance to normalize. May be null, in which case, the returned value is that of calling
   * {@link #nullValue()}
   * @return a {@code Value} instance for the specified object.
   */
  public Value valueOf(Object object);

  /**
   * Returns a {@code ValueSequence} instance containing the specified {@code values}.
   * 
   * @param values the sequence of {@code Value} instances to hold in the {@code ValueSequence}. May be null, in which
   * case, the return value is that of calling {@link #nullSequence()}
   * 
   * @return a {@code ValueSequence} instance containing {@code values}
   */
  public ValueSequence sequenceOf(Iterable<Value> values);

  public ValueSequence sequenceOf(String values);

}

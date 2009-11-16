package org.obiba.magma;

import java.io.Serializable;

public interface ValueType extends Serializable {

  public static class Factory {

    public static ValueType forName(String name) {
      return MetaEngine.get().getValueTypeFactory().forName(name);
    }

    public static ValueType forClass(Class<?> javaClass) {
      return MetaEngine.get().getValueTypeFactory().forClass(javaClass);
    }

    public static Value newValue(ValueType type, Serializable value) {
      return new Value(type, value);
    }
  }

  /**
   * The unique name of this {@code ValueType}
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
   * @return true if the {@code #valueOf(Object)} can be called with an instance of the specified class.
   */
  public boolean acceptsJavaClass(Class<?> clazz);

  /**
   * Returns true if this type represents a date, time or both
   * @return
   */
  public boolean isDateTime();

  /**
   * Returns true if this type represents a number
   * @return
   */
  public boolean isNumeric();

  /**
   * Returns a {@code Value} instance that represents the null value for this type
   * @return a {@code Value} instance for null
   */
  public Value nullValue();

  /**
   * Returns a string representation of the {@code value}. The string returned can be passed to the {@code
   * #valueOf(String)} method which should return an equivalent {@code Value} instance.
   * @param value the value to convert to a string
   * @return a {@code String} representation of the {@code value}.
   */
  public String toString(Value value);

  /**
   * Converts a string representation of a {@code value} to a {@code Value} instance. The string representation should
   * match the expected format which is specified by the {@code #toString(Value))} method.
   * 
   * @param string a string representation of the vale
   * @return a {@code Value} instance
   */
  public Value valueOf(String string);

  /**
   * Builds a {@code Value} instance after converting the specified object to the normalized type returned by {@code
   * #getJavaClass()} method. Note that this method accepts null values and should return a non-null {@code Value}
   * instance.
   * <p/>
   * For example, this method would convert instances of {@code java.util.Date}, {@code java.sql.Date}, {@code
   * java.sql.Timestamp}, {@code java.util.Calendar} to an instance of {@code java.util.Date} and return a {@code Value}
   * instance containing the normalized instance.
   * 
   * @param object the instance to normalize
   * @return a {@code Value} instance with the specified value
   */
  public Value valueOf(Object object);

}

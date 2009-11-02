package org.obiba.meta;

import java.io.Serializable;

import javax.xml.namespace.QName;

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

  public String getName();

  public QName getXsdType();

  public Class<?> getJavaClass();

  public boolean acceptsJavaClass(Class<?> clazz);

  public boolean isDateTime();

  public boolean isNumeric();

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

  public Value valueOf(Object object);

}

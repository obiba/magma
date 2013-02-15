package org.obiba.magma.type;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;

import au.com.bytecode.opencsv.CSVParser;

public class TextType extends AbstractValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  protected static final String QUOTE_STR = "" + QUOTE;

  protected static final String ESCAPED_QUOTE_STR = "" + QUOTE + QUOTE;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<TextType> instance;

  private transient CSVParser csvParser;

  protected TextType() {
  }

  public static TextType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new TextType());
    }
    return instance.get();
  }

  @Override
  public String getName() {
    return "text";
  }

  @Override
  public Class<?> getJavaClass() {
    return String.class;
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return String.class.isAssignableFrom(clazz) || clazz.isEnum();
  }

  @Override
  public boolean isDateTime() {
    return false;
  }

  @Override
  public boolean isNumeric() {
    return false;
  }

  @Override
  public Value valueOf(String string) {
    if(string == null) {
      return nullValue();
    }
    return Factory.newValue(this, string);
  }

  @Override
  public Value valueOf(Object object) {
    if(object == null) {
      return nullValue();
    }
    return Factory.newValue(this, object.toString());
  }

  /**
   * Reads a comma-separated string value of strings. The format of the string is
   * <p/>
   * <pre>
   * "value","value","value"
   * </pre>
   * <p/>
   * When the original {@code value} contains a {@code "}, it is escaped by adding another {@code "}, as per the CSV
   * standard.
   */
  @Override
  public ValueSequence sequenceOf(String string) {
    if(string == null) {
      return nullSequence();
    }
    List<Value> values = new ArrayList<Value>();

    // Special case for empty string
    if(string.isEmpty()) {
      values.add(valueOf(string));
      return sequenceOf(values);
    }

    try {
      for(String currentValue : getCsvParser().parseLine(string)) {
        values.add(valueOf(currentValue.length() == 0 ? null : currentValue.toString()));
      }
    } catch(IOException e) {
      throw new MagmaRuntimeException("Invalid value sequence formatting: " + string, e);
    }

    return sequenceOf(values);
  }

  /**
   * Adds quotes around the string value and also escapes any quotes in the value by prefixing it with another quote.
   * This is the format expected by the {@code sequenceOf(String string)} method.
   */
  @Override
  protected String escapeAndQuoteIfRequired(String value) {
    // Replace all occurrences of QUOTE by QUOTEQUOTE
    return QUOTE + value.replaceAll(QUOTE_STR, ESCAPED_QUOTE_STR) + QUOTE;
  }

  @Override
  public int compare(Value o1, Value o2) {
    return ((String) o1.getValue()).compareTo((String) o2.getValue());
  }

  private CSVParser getCsvParser() {
    if (csvParser == null) {
      csvParser = new CSVParser(SEPARATOR, QUOTE);
    }
    return csvParser;
  }

}

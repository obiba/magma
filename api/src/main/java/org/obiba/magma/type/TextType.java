package org.obiba.magma.type;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;

import com.google.common.base.Strings;

import au.com.bytecode.opencsv.CSVParser;

public class TextType extends AbstractValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  protected static final String QUOTE_STR = "" + QUOTE;

  protected static final String ESCAPED_QUOTE_STR = "" + QUOTE + QUOTE;

  @SuppressWarnings("StaticNonFinalField")
  @Nullable
  private static WeakReference<TextType> instance;

  private transient CSVParser csvParser;

  protected TextType() {
  }

  @SuppressWarnings("ConstantConditions")
  @Nonnull
  public static TextType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new TextType());
    }
    return instance.get();
  }

  @Nonnull
  @Override
  public String getName() {
    return "text";
  }

  @Override
  public Class<?> getJavaClass() {
    return String.class;
  }

  @Override
  public boolean acceptsJavaClass(@Nonnull Class<?> clazz) {
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

  @Nonnull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    return Factory.newValue(this, string);
  }

  @Nonnull
  @Override
  public Value valueOf(@Nullable Object object) {
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
  @Nonnull
  @Override
  public ValueSequence sequenceOf(@Nullable String string) {
    if(string == null) {
      return nullSequence();
    }
    Collection<Value> values = new ArrayList<Value>();

    // Special case for empty string
    if(string.isEmpty()) {
      values.add(valueOf(string));
      return sequenceOf(values);
    }

    try {
      for(String currentValue : getCsvParser().parseLine(string)) {
        values.add(valueOf(currentValue.isEmpty() ? null : currentValue));
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
  @Nullable
  @Override
  protected String escapeAndQuoteIfRequired(@Nullable String value) {
    String escaped = value == null ? "" : value;
    // Replace all occurrences of " by ""
    escaped = escaped.replaceAll(QUOTE_STR, ESCAPED_QUOTE_STR);
    return QUOTE + escaped + QUOTE;
  }

  @Override
  public int compare(Value o1, Value o2) {
    String s1 = (String) o1.getValue();
    String s2 = (String) o2.getValue();
    return Strings.nullToEmpty(s1).compareTo(Strings.nullToEmpty(s2));
  }

  private CSVParser getCsvParser() {
    if(csvParser == null) {
      csvParser = new CSVParser(SEPARATOR, QUOTE);
    }
    return csvParser;
  }

}

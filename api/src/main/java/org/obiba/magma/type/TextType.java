package org.obiba.magma.type;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;

public class TextType extends AbstractValueType {

  private static final long serialVersionUID = -5271259966499174607L;

  protected static final String QUOTE_STR = "" + QUOTE;

  protected static final String ESCAPED_QUOTE_STR = "" + QUOTE + QUOTE;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<TextType> instance;

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
   * 
   * <pre>
   * "value","value","value"
   * </pre>
   * 
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

    StringBuilder currentValue = new StringBuilder();
    // Whether we're currently inside quotes
    boolean inQuotes = false;
    // Where the current opening quote is located
    int openingQuoteIndex = -1;
    for(int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if(c == QUOTE) {
        // If the current character is quote, there are three possibilities:
        // 1) A new quoted value is starting
        // 2) The current string value is ending
        // 3) Escaped quote

        if(inQuotes) {
          // Either case 2) or 3): the end of the quoted string or an escaped quote
          // 2 possibilities for case 2):
          // 2a) End of string (no more characters)
          // 2b) End of value (next character is a SEPARATOR)
          // Case 3): next character must be a QUOTE
          // Anything else is unexpected

          if(i + 1 >= string.length()) {
            // 2a) End of string
            inQuotes = false;
          } else {
            // The next character should either be a quote or a separator
            char nextChar = string.charAt(i + 1);
            if(nextChar == SEPARATOR) {
              // 2b) End of value
              inQuotes = false;
            } else if(nextChar == QUOTE) {
              // 3) Escaped quote
              currentValue.append(QUOTE);
              // skip extra quote
              i++;
            } else {
              // anything else is unexpected
              throw new IllegalArgumentException("Invalid string value. Unexpected escape character at index " + i + ": '" + string + "'");
            }
          }
        } else {
          // Case 1): we weren't in a quoted string and from this point on, we are.
          inQuotes = true;
          openingQuoteIndex = i;
        }
      } else if(c == SEPARATOR && inQuotes == false) {
        // When we're outside quotes and we encounter a SEPARATOR, then we've finished reading a value.
        values.add(valueOf(currentValue.length() == 0 ? null : currentValue.toString()));
        currentValue.setLength(0);
      } else if(inQuotes == true) {
        // When inside quotes, the character is appended to the value
        currentValue.append(c);
      } else {
        // We've encountered a character that is not a separator outside quotes. This is unexpected, but not a show
        // stopper. We can keep parsing.
      }
    }
    // We've finished reading all values.
    if(inQuotes == true) {
      // Still inside quoted string. This is bad.
      throw new IllegalArgumentException("Unterminated string. Opening quote at index " + openingQuoteIndex + " was never closed: '..." + string.substring(openingQuoteIndex) + "'");
    }
    // Add the last value to the sequence.
    values.add(valueOf(currentValue.length() == 0 ? null : currentValue.toString()));
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
}

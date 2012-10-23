package org.obiba.magma.type;

import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.ValueLoader;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public abstract class AbstractValueType implements ValueType {

  private static final long serialVersionUID = -2655334789781837332L;

  protected static final char SEPARATOR = ',';

  protected static final char QUOTE = '"';

  protected final Value nullValue;

  protected final ValueSequence nullSequence;

  protected AbstractValueType() {
    nullValue = Factory.newValue(this, null);
    nullSequence = Factory.newSequence(this, null);
  }

  @Override
  public Value nullValue() {
    return nullValue;
  }

  @Override
  public ValueSequence nullSequence() {
    return nullSequence;

  }

  @Override
  public Value valueOf(ValueLoader loader) {
    return Factory.newValue(this, loader);
  }

  @Override
  public ValueSequence sequenceOf(Iterable<Value> values) {
    return Factory.newSequence(this, values);
  }

  @Override
  public ValueSequence sequenceOf(String string) {
    if(string == null) {
      return nullSequence();
    }
    List<Value> values = new ArrayList<Value>();
    StringBuilder currentValue = new StringBuilder();
    for(int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if(c == SEPARATOR) {
        values.add(valueOf(currentValue.length() == 0 ? null : currentValue.toString()));
        currentValue.setLength(0);
      } else {
        currentValue.append(c);
      }
    }
    if(currentValue.length() > 0) {
      values.add(valueOf(currentValue.toString()));
    }
    return sequenceOf(values);
  }

  @Override
  public Value convert(Value value) {
    if(value.getValueType() == this) {
      return value;
    }
    if(value.isNull()) {
      return value.isSequence() ? nullSequence() : nullValue();
    }
    final ValueConverter converter = Factory.conveterFor(value.getValueType(), this);
    if(value.isSequence()) {
      return sequenceOf(Iterables.transform(value.asSequence().getValue(), new Function<Value, Value>() {

        @Override
        public Value apply(Value from) {
          return converter.convert(from, AbstractValueType.this);
        }

      }));
    } else {
      return converter.convert(value, this);
    }
  }

  @Override
  public String toString(Value value) {
    return value.isNull() ? null : (value.isSequence() ? toString(value.asSequence()) : toString(value.getValue()));
  }

  /**
   * Allows {@code ValueType} instance to apply formatting or specialised conversion to string representation. For
   * example, dates would be formatted in a non-locale dependent way.
   * 
   * @param object a non-null object
   * @return a {@code String} representation of the object
   */
  protected String toString(Object object) {
    return object.toString();
  }

  /**
   * Returns a comma-separated string representation of the sequence. The resulting string can be passed to
   * {@code sequenceOf(String)} to obtain the original {@code ValueSequence}.
   * 
   * @param sequence
   * @return
   */
  protected String toString(ValueSequence sequence) {
    final StringBuilder sb = new StringBuilder();
    for(Value value : sequence.getValue()) {
      sb.append(value.isNull() ? "" : escapeAndQuoteIfRequired(value.toString())).append(SEPARATOR);
    }
    // Remove the last separator
    if(sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  protected String escapeAndQuoteIfRequired(String value) {
    return value;
  }
}

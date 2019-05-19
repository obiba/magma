/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.obiba.magma.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class AbstractValueType implements ValueType {

  private static final long serialVersionUID = -2655334789781837332L;

//  private static final Logger log = LoggerFactory.getLogger(AbstractValueType.class);

  protected static final char SEPARATOR = ',';

  protected static final char QUOTE = '"';

  protected final Value nullValue;

  protected final ValueSequence nullSequence;

  protected AbstractValueType() {
    nullValue = Factory.newValue(this, null);
    nullSequence = Factory.newSequence(this, null);
  }

  @Override
  public boolean isGeo() {
    return false;
  }

  @Override
  public boolean isBinary() {
    return false;
  }

  @NotNull
  @Override
  public Value nullValue() {
    return nullValue;
  }

  @NotNull
  @Override
  public ValueSequence nullSequence() {
    return nullSequence;

  }

  @NotNull
  @Override
  public Value valueOf(@Nullable ValueLoader loader) {
    return Factory.newValue(this, loader);
  }

  @NotNull
  @Override
  public ValueSequence sequenceOf(@Nullable Iterable<Value> values) {
    return Factory.newSequence(this, values);
  }

  @NotNull
  @Override
  public ValueSequence sequenceOf(@Nullable String string) {
    if(string == null) {
      return nullSequence();
    }
    Collection<Value> values = new ArrayList<>();
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

  @NotNull
  @SuppressWarnings("IfMayBeConditional")
  @Override
  public Value convert(@NotNull Value value) {
    if(value.getValueType() == this) {
      return value;
    }
    if(value.isNull()) {
      return value.isSequence() ? nullSequence() : nullValue();
    }
    final ValueConverter converter = Factory.converterFor(value.getValueType(), this);
    if(value.isSequence()) {
      return sequenceOf(
          Lists.newArrayList(Iterables.transform(value.asSequence().getValue(), new Function<Value, Value>() {

            @Override
            public Value apply(Value from) {
              return converter.convert(from, AbstractValueType.this);
            }
          })));
    }
    return converter.convert(value, this);
  }

  @Nullable
  @Override
  public String toString(@Nullable Value value) {
    return value == null || value.isNull() //
        ? null //
        : value.isSequence() ? toString(value.asSequence()) : toString(value.getValue());
  }

  /**
   * Allows {@code ValueType} instance to apply formatting or specialised conversion to string representation. For
   * example, dates would be formatted in a non-locale dependent way.
   *
   * @param object a non-null object
   * @return a {@code String} representation of the object
   */
  @Nullable
  protected String toString(@Nullable Object object) {
    return object == null ? null : object.toString();
  }

  /**
   * Returns a comma-separated string representation of the sequence. The resulting string can be passed to
   * {@code sequenceOf(String)} to obtain the original {@code ValueSequence}.
   *
   * @param sequence
   * @return
   */
  @SuppressWarnings("ConstantConditions")
  @Nullable
  protected String toString(@Nullable ValueSequence sequence) {
    if(sequence == null || sequence.isNull()) return null;
    StringBuilder sb = new StringBuilder();
    for(Value value : sequence.getValue()) {
      sb.append(value.isNull() ? "" : escapeAndQuoteIfRequired(value.toString())).append(SEPARATOR);
    }
    // Remove the last separator
    int length = sb.length();
    if(length > 0) {
      sb.deleteCharAt(length - 1);
    }
    return sb.toString();
  }

  @Nullable
  protected String escapeAndQuoteIfRequired(@Nullable String value) {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) return false;
    if (other == this) return true;
    if (!(other instanceof ValueType)) return false;
    ValueType otherType = (ValueType) other;
    // compare type name instead of instances (in case there is not a single instance as expected)
    return Objects.equals(getName(), otherType.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }
}

/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.support.ValueComparator;

public class BooleanType extends AbstractValueType {

  private static final long serialVersionUID = -149385659514790222L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<BooleanType> instance;

  private final Value trueValue;

  private final Value falseValue;

  private BooleanType() {
    trueValue = Factory.newValue(this, Boolean.TRUE);
    falseValue = Factory.newValue(this, Boolean.FALSE);
  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @NotNull
  public static BooleanType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new BooleanType());
    }
    return instance.get();
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
  public Class<?> getJavaClass() {
    return Boolean.class;
  }

  @NotNull
  @Override
  public String getName() {
    return "boolean";
  }

  @Override
  public boolean acceptsJavaClass(@NotNull Class<?> clazz) {
    return Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz);
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    if("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)) {
      return valueOf(Boolean.valueOf(string.toLowerCase()).booleanValue());
    }
    if("T".equalsIgnoreCase(string)) {
      return valueOf(true);
    }
    if("F".equalsIgnoreCase(string)) {
      return valueOf(false);
    }
    throw new IllegalArgumentException(
        "Cannot construct " + getClass().getSimpleName() + " from type String with value '" + string + "'");
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null) {
      return nullValue();
    }
    if(object instanceof Boolean) {
      return valueOf(((Boolean) object).booleanValue());
    }
    if(boolean.class.isAssignableFrom(object.getClass())) {
      return valueOf(boolean.class.cast(object));
    }
    if(object instanceof Value) {
      return convert((Value)object);
    }
    if(object instanceof Value && ((Value)object).isSequence()) {
      return sequenceOf(((Value)object).asSequence().getValues());
    }
    return valueOf(object.toString());
  }

  public Value valueOf(Boolean object) {
    if(object == null) {
      return nullValue();
    }
    return valueOf(object.booleanValue());
  }

  public Value valueOf(boolean value) {
    return value ? trueValue : falseValue;
  }

  public Value trueValue() {
    return trueValue;
  }

  public Value falseValue() {
    return falseValue;
  }

  public Value not(Value value) {
    if(value.getValueType() != this) {
      throw new IllegalArgumentException("value is not of BooleanType: " + value);
    }
    if(value.isNull()) {
      return value;
    }
    if(trueValue.equals(value)) {
      return falseValue;
    }
    if(falseValue.equals(value)) {
      return trueValue;
    }
    // This really isn't possible
    throw new IllegalArgumentException("value of BooleanType is neither true nor false: " + value);
  }

  @Override
  public int compare(Value o1, Value o2) {
    return ValueComparator.INSTANCE.compare(o1, o2);
  }
}

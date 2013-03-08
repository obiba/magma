package org.obiba.magma.type;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;

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

  @Override
  public String getName() {
    return "boolean";
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz);
  }

  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    if("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)) {
      return valueOf(Boolean.valueOf(string).booleanValue());
    }
    throw new IllegalArgumentException(
        "Cannot construct " + getClass().getSimpleName() + " from type String with value '" + string + "'");
  }

  @Override
  public Value valueOf(Object object) {
    if(object == null) {
      return nullValue();
    }
    if(object instanceof Boolean) {
      return valueOf(((Boolean) object).booleanValue());
    }
    if(boolean.class.isAssignableFrom(object.getClass())) {
      return valueOf(boolean.class.cast(object));
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
    return ((Boolean) o1.getValue()).compareTo((Boolean) o2.getValue());
  }
}

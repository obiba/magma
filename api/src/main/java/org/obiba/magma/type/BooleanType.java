package org.obiba.magma.type;

import java.lang.ref.WeakReference;

import org.obiba.magma.MetaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

public class BooleanType implements ValueType {

  private static final long serialVersionUID = -149385659514790222L;

  private static WeakReference<BooleanType> instance;

  private Value trueValue;

  private Value falseValue;

  private BooleanType() {
    trueValue = Factory.newValue(this, new Boolean(true));
    falseValue = Factory.newValue(this, new Boolean(false));
  }

  public static BooleanType get() {
    if(instance == null || instance.get() == null) {
      instance = MetaEngine.get().registerInstance(new BooleanType());
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
  public Value nullValue() {
    return Factory.newValue(this, null);
  }

  @Override
  public String toString(Value value) {
    return value.isNull() ? null : value.getValue().toString();
  }

  @Override
  public Value valueOf(String string) {
    return Factory.newValue(this, Boolean.valueOf(string));
  }

  @Override
  public Value valueOf(Object object) {
    String str = object != null ? object.toString() : null;
    return Factory.newValue(this, Boolean.valueOf(str));
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
}

package org.obiba.magma.type;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;

public class DecimalType extends AbstractNumberType {

  private static final long serialVersionUID = -149385659514790222L;

  private static WeakReference<DecimalType> instance;

  private DecimalType() {

  }

  public static DecimalType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new DecimalType());
    }
    return instance.get();
  }

  @Override
  public Class<?> getJavaClass() {
    return BigDecimal.class;
  }

  @Override
  public String getName() {
    return "decimal";
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz) || BigDecimal.class.isAssignableFrom(clazz);
  }

  @Override
  public Value valueOf(String string) {
    return Factory.newValue(this, Integer.valueOf(string));
  }

  @Override
  public Value valueOf(Object object) {
    if(object == null) {
      return Factory.newValue(this, null);
    }
    Class<?> type = object.getClass();
    if(Number.class.isAssignableFrom(type)) {
      return Factory.newValue(this, Double.valueOf(((Number) object).doubleValue()));
    } else if(type.isPrimitive()) {
      throw new UnsupportedOperationException();
    }
    throw new IllegalArgumentException("Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }
}

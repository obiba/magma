package org.obiba.magma.type;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

import javax.annotation.Nullable;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
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
    return Double.class;
  }

  @Override
  public String getName() {
    return "decimal";
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz) ||
        Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz) ||
        BigDecimal.class.isAssignableFrom(clazz);
  }

  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    try {
      return Factory.newValue(this, Double.valueOf(normalize(string)));
    } catch(NumberFormatException e) {
      throw new MagmaRuntimeException("Not a decimal value: " + string, e);
    }
  }

  @Override
  public Value valueOf(Object object) {
    if(object == null) {
      return nullValue();
    }
    Class<?> type = object.getClass();
    if(Number.class.isAssignableFrom(type)) {
      return Factory.newValue(this, Double.valueOf(((Number) object).doubleValue()));
    }
    if(String.class.isAssignableFrom(type)) {
      return valueOf((String) object);
    }
    throw new IllegalArgumentException(
        "Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }

  @Override
  public int compare(Value o1, Value o2) {
    return ((Double) o1.getValue()).compareTo((Double) o2.getValue());
  }

  private String normalize(String string) {
    return string.replace(",", ".").trim();
  }
}

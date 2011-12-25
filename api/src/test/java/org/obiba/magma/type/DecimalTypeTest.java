package org.obiba.magma.type;

import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

public class DecimalTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return DecimalType.get();
  }

  @Override
  Object getObjectForType() {
    return new Double(78372.543543d);
  }

  @Override
  boolean isDateTime() {
    return false;
  }

  @Override
  boolean isNumeric() {
    return true;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    return ImmutableList.<Class<?>> of(double.class, Double.class, float.class, Float.class);
  }

}

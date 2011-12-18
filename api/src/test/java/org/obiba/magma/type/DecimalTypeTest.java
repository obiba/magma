package org.obiba.magma.type;

import org.obiba.magma.ValueType;

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
}

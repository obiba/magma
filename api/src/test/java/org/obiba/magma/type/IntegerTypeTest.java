package org.obiba.magma.type;

import org.obiba.magma.ValueType;

public class IntegerTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return IntegerType.get();
  }

  @Override
  Object getObjectForType() {
    return new Long(42);
  }
}

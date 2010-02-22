package org.obiba.magma.type;

import org.obiba.magma.ValueType;

public class BooleanTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return BooleanType.get();
  }

  @Override
  Object getObjectForType() {
    return new Boolean(true);
  }
}

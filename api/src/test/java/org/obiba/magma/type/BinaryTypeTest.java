package org.obiba.magma.type;

import org.junit.Ignore;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

public class BinaryTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return BinaryType.get();
  }

  @Override
  Object getObjectForType() {
    return new byte[] { 1, 2, 3, 4 };
  }

  @Override
  boolean isDateTime() {
    return false;
  }

  @Override
  boolean isNumeric() {
    return false;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    return ImmutableList.<Class<?>> of(byte[].class);
  }

  @Ignore("equals() on arrays does not compare array contents. We need to override the value.equals() method for BinaryType.")
  public void testValueOfToStringIsEqual() {
  }

  @Ignore("equals() on arrays does not compare array contents. We need to override the value.equals() method for BinaryType.")
  public void testValueOfToStringSequence() {
  }
}

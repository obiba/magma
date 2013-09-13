package org.obiba.magma.type;

import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DecimalTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return DecimalType.get();
  }

  @Override
  Object getObjectForType() {
    return 78372.543543d;
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
    return ImmutableList.<Class<?>>of(double.class, Double.class, float.class, Float.class);
  }

  @Test
  public void testTrim() {
    Double result = (Double) getValueType().valueOf(" 1 ").getValue();
    assertThat(result.intValue(), is(1));
  }

  @Test
  public void test_compare_with_null() throws Exception {
    Value leftValue = getValueType().valueOf(42);
    Value rightValue = getValueType().nullValue();
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result, is(1));
  }

  @Test
  public void testComma() {
    Double result = (Double) getValueType().valueOf("1,2").getValue();
    assertThat(result, is(1.2));
  }

}

package org.obiba.magma.type;

import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IntegerTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return IntegerType.get();
  }

  @Override
  Object getObjectForType() {
    return new Long(42);
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
    return ImmutableList.<Class<?>>of(int.class, Integer.class, long.class, Long.class);
  }

  @Test(expected = ClassCastException.class)
  public void testCompareWithRightArgumentOfWrongValueType() throws Exception {
    Value leftValue = getValueType().valueOf(getObjectForType());
    Value rightValue = TextType.get().valueOf("wrongType");
    getValueType().compare(leftValue, rightValue);
  }

  @Test(expected = ClassCastException.class)
  public void testCompareWithLeftArgumentOfWrongValueType() throws Exception {
    Value leftValue = TextType.get().valueOf("wrongType");
    Value rightValue = getValueType().valueOf(getObjectForType());
    getValueType().compare(leftValue, rightValue);
  }

  @Test
  public void testCompareWithLeftArgumentLessThanRightArgument() throws Exception {
    Value leftValue = getValueType().valueOf(40);
    Value rightValue = getValueType().valueOf(42);
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result, is(-1));
  }

  @Test
  public void testCompareWithLeftArgumentGreaterThanRightArgument() throws Exception {
    Value leftValue = getValueType().valueOf(44);
    Value rightValue = getValueType().valueOf(42);
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result, is(1));
  }

  @Test
  public void testCompareWithEqualArguments() throws Exception {
    Value leftValue = getValueType().valueOf(42);
    Value rightValue = getValueType().valueOf(42);
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result, is(0));
  }

  @Test
  public void test_compare_with_null() throws Exception {
    Value leftValue = getValueType().valueOf(42);
    Value rightValue = getValueType().nullValue();
    int result = getValueType().compare(leftValue, rightValue);
    assertThat(result, is(1));
  }

  @Test
  public void testTrim() {
    Long result = (Long) getValueType().valueOf(" 1 ").getValue();
    assertThat(result.intValue(), is(1));
  }
}

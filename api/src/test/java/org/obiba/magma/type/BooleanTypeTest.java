package org.obiba.magma.type;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.magma.Value;

import com.google.common.collect.ImmutableList;

public class BooleanTypeTest extends BaseValueTypeTest {

  @Override
  BooleanType getValueType() {
    return BooleanType.get();
  }

  @Override
  Object getObjectForType() {
    return new Boolean(true);
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
    return ImmutableList.<Class<?>> of(boolean.class, Boolean.class);
  }

  @Test
  public void test_valueOf_Boolean() {
    Value value = getValueType().valueOf(new Boolean(true));
    Assert.assertEquals(getValueType().trueValue(), value);
    value = getValueType().valueOf(new Boolean(false));
    Assert.assertEquals(getValueType().falseValue(), value);
    value = getValueType().valueOf((Boolean) null);
    Assert.assertEquals(value.isNull(), true);
  }

  @Test
  public void test_valueOf_string() {
    Value value = getValueType().valueOf((Object) "false");
    Assert.assertEquals(getValueType().falseValue(), value);
    value = getValueType().valueOf((Object) "true");
    Assert.assertEquals(getValueType().trueValue(), value);
  }

  @Test
  public void test_not_true() {
    Value value = getValueType().not(getValueType().trueValue());
    Assert.assertEquals(getValueType().falseValue(), value);
  }

  @Test
  public void test_not_false() {
    Value value = getValueType().not(getValueType().falseValue());
    Assert.assertEquals(getValueType().trueValue(), value);
  }

  @Test
  public void test_not_null() {
    Value value = getValueType().not(getValueType().nullValue());
    Assert.assertEquals(getValueType().nullValue(), value);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_not_onlyAcceptsBoolenType() {
    getValueType().not(TextType.get().valueOf("not a boolean"));
  }
}

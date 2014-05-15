package org.obiba.magma.type;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings("ReuseOfLocalVariable")
public class BooleanTypeTest extends BaseValueTypeTest {

  @Override
  BooleanType getValueType() {
    return BooleanType.get();
  }

  @Override
  Object getObjectForType() {
    return Boolean.TRUE;
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
    return ImmutableList.<Class<?>>of(boolean.class, Boolean.class);
  }

  @Test
  public void test_valueOf_Boolean() {
    assertThat(getValueType().valueOf(Boolean.TRUE)).isEqualTo(getValueType().trueValue());
    assertThat(getValueType().valueOf(Boolean.FALSE)).isEqualTo(getValueType().falseValue());
    assertThat(getValueType().valueOf((Boolean) null).isNull()).isTrue();
  }

  @Test
  public void test_valueOf_string() {
    assertThat(getValueType().valueOf((Object) "false")).isEqualTo(getValueType().falseValue());
    assertThat(getValueType().valueOf((Object) "true")).isEqualTo(getValueType().trueValue());
  }

  @Test
  public void test_not_true() {
    assertThat(getValueType().not(getValueType().trueValue())).isEqualTo(getValueType().falseValue());
  }

  @Test
  public void test_not_false() {
    assertThat(getValueType().not(getValueType().falseValue())).isEqualTo(getValueType().trueValue());
  }

  @Test
  public void test_not_null() {
    assertThat(getValueType().not(getValueType().nullValue())).isEqualTo(getValueType().nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_not_onlyAcceptsBoolenType() {
    getValueType().not(TextType.get().valueOf("not a boolean"));
  }
}

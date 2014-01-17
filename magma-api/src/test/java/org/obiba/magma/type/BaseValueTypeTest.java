package org.obiba.magma.type;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.test.AbstractMagmaTest;

import static org.fest.assertions.api.Assertions.assertThat;

public abstract class BaseValueTypeTest extends AbstractMagmaTest {

  abstract ValueType getValueType();

  abstract Object getObjectForType();

  abstract boolean isNumeric();

  abstract boolean isDateTime();

  abstract Iterable<Class<?>> validClasses();

  ValueSequence getSequence(int size) {
    Collection<Value> values = new ArrayList<>(size);
    for(int i = 0; i < size; i++) {
      values.add(getValueType().valueOf(getObjectForType()));
    }
    return getValueType().sequenceOf(values);
  }

  @Before
  public void validateTestInstance() {
    assertThat(getValueType()).isNotNull();
    assertThat(getObjectForType()).isNotNull();
  }

  @Test
  public void testNameIsNotEmpty() {
    assertThat(getValueType().getName()).isNotNull();
    assertThat(getValueType().getName().length() > 0).isTrue();
  }

  @Test
  public void testJavaTypeNotEmpty() {
    Class<?> javaClass = getValueType().getJavaClass();
    assertThat(javaClass).isNotNull();
    assertThat(getValueType().acceptsJavaClass(javaClass)).isTrue();
  }

  @Test
  public void testNullValueNotNull() {
    Value nullValue = getValueType().nullValue();
    assertThat(nullValue).isNotNull();
    assertThat(nullValue.isNull()).isTrue();
  }

  @Test
  public void testToStringOfNullValue() {
    Value nullValue = getValueType().nullValue();
    String nullString = nullValue.toString();
    assertThat(nullString).isNull();
  }

  @Test
  public void testValueOfNullString() {
    Value nullValue = getValueType().valueOf((String) null);
    assertThat(nullValue).isNotNull();
    assertThat(nullValue.isNull()).isTrue();
  }

  @Test
  public void testValueOfNullObject() {
    Value nullValue = getValueType().valueOf((Object) null);
    assertThat(nullValue).isNotNull();
    assertThat(nullValue.isNull()).isTrue();
  }

  @Test
  public void testValueFromObjectIsNotNull() {
    Object valueObject = getObjectForType();
    Value value = getValueType().valueOf(valueObject);
    assertThat(value).isNotNull();
    assertThat(value.isNull()).isFalse();
    assertThat(value.isSequence()).isFalse();
  }

  @Test
  public void testValueObjectIsEqual() {
    Object valueObject = getObjectForType();
    Value value = getValueType().valueOf(valueObject);
    assertThat(valueObject).isEqualTo(value.getValue());
  }

  @Test
  public void testToStringOfValueObjectIsNotNull() {
    Object valueObject = getObjectForType();
    Value value = getValueType().valueOf(valueObject);
    assertThat(value.toString()).isNotNull();
  }

  @Test
  public void testValueOfToStringIsEqual() {
    Object valueObject = getObjectForType();
    Value value = getValueType().valueOf(valueObject);
    String strValue = value.toString();
    Value valueOf = getValueType().valueOf(strValue);

    assertThat(valueOf).isNotNull();
    assertThat(value).isEqualTo(valueOf);
  }

  @Test
  public void testValueOfToStringSequence() {
    ValueSequence sequence = getSequence(5);

    String strValue = sequence.toString();
    Value valueOf = getValueType().sequenceOf(strValue);

    assertThat(valueOf).isNotNull();
    assertThat(sequence).isEqualTo((ValueSequence) valueOf);
  }

  @Test(expected = NullPointerException.class)
  public void testCompareWithNullArguments() throws Exception {
    getValueType().compare(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCompareWithLeftNullArgument() throws Exception {
    Value value = getValueType().valueOf(getObjectForType());
    getValueType().compare(null, value);
  }

  @Test(expected = NullPointerException.class)
  public void testCompareWithRightNullArgument() throws Exception {
    Value value = getValueType().valueOf(getObjectForType());
    getValueType().compare(value, null);
  }

  @Test
  public void test_isNumeric() {
    assertThat(isNumeric()).isEqualTo(getValueType().isNumeric());
  }

  @Test
  public void test_isDateTime() {
    assertThat(isDateTime()).isEqualTo(getValueType().isDateTime());
  }

  @Test
  public void test_acceptsJavaClass() {
    for(Class<?> validClass : validClasses()) {
      assertThat(getValueType().acceptsJavaClass(validClass)).isTrue();
    }
  }

  @Test
  public void test_acceptsJavaClass_doesNotAcceptInvalidClass() {
    assertThat(getValueType().acceptsJavaClass(NullPointerException.class)).isFalse();
  }
}

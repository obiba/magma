package org.obiba.magma.type;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueType;
import org.obiba.magma.test.AbstractMagmaTest;

public abstract class BaseValueTypeTest extends AbstractMagmaTest {

  abstract ValueType getValueType();

  abstract Object getObjectForType();

  ValueSequence getSequence(int size) {
    List<Value> values = new ArrayList<Value>(size);
    for(int i = 0; i < size; i++) {
      values.add(getValueType().valueOf(getObjectForType()));
    }
    return getValueType().sequenceOf(values);
  }

  @Before
  public void validateTestInstance() {
    Assert.assertNotNull(getValueType());
    Assert.assertNotNull(getObjectForType());
  }

  @Test
  public void testNameIsNotEmpty() {
    Assert.assertNotNull(getValueType().getName());
    Assert.assertTrue(getValueType().getName().length() > 0);
  }

  @Test
  public void testJavaTypeNotEmpty() {
    Class<?> javaClass = getValueType().getJavaClass();
    Assert.assertNotNull(javaClass);
    Assert.assertTrue(getValueType().acceptsJavaClass(javaClass));
  }

  @Test
  public void testNullValueNotNull() {
    Value nullValue = getValueType().nullValue();
    Assert.assertNotNull(nullValue);
    Assert.assertTrue(nullValue.isNull());
  }

  @Test
  public void testToStringOfNullValue() {
    Value nullValue = getValueType().nullValue();
    String nullString = nullValue.toString();
    Assert.assertNull(nullString);
  }

  @Test
  public void testValueOfNullString() {
    Value nullValue = getValueType().valueOf((String) null);
    Assert.assertNotNull(nullValue);
    Assert.assertTrue(nullValue.isNull());
  }

  @Test
  public void testValueOfNullObject() {
    Value nullValue = getValueType().valueOf((Object) null);
    Assert.assertNotNull(nullValue);
    Assert.assertTrue(nullValue.isNull());
  }

  @Test
  public void testValueFromObjectIsNotNull() {
    Object valueObject = getObjectForType();
    Value value = getValueType().valueOf(valueObject);
    Assert.assertNotNull(value);
    Assert.assertFalse(value.isNull());
    Assert.assertFalse(value.isSequence());
  }

  @Test
  public void testValueObjectIsEqual() {
    Object valueObject = getObjectForType();
    Value value = getValueType().valueOf(valueObject);
    Assert.assertEquals(valueObject, value.getValue());
  }

  @Test
  public void testToStringOfValueObjecttIsNotNull() {
    Object valueObject = getObjectForType();
    Value value = getValueType().valueOf(valueObject);
    Assert.assertNotNull(value.toString());
  }

  @Test
  public void testValueOfToStringIsEqual() {
    Object valueObject = getObjectForType();
    Value value = getValueType().valueOf(valueObject);
    String strValue = value.toString();
    Value valueOf = getValueType().valueOf(strValue);

    Assert.assertNotNull(valueOf);
    Assert.assertEquals(value, valueOf);
  }

  @Test
  public void testValueOfToStringSequence() {
    ValueSequence sequence = getSequence(5);

    String strValue = sequence.toString();
    Value valueOf = getValueType().sequenceOf(strValue);

    Assert.assertNotNull(valueOf);
    Assert.assertEquals(sequence, valueOf);
  }
}

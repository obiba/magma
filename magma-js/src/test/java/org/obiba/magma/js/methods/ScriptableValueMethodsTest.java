package org.obiba.magma.js.methods;

import java.util.Date;
import java.util.Locale;

import org.junit.Test;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;

import junit.framework.Assert;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mozilla.javascript.Context.getCurrentContext;

public class ScriptableValueMethodsTest extends AbstractJsTest {

  // @Test
  // public void testValueForNull() {
  // ScriptableValue textValue = newValue(TextType.get().valueOf("Text value"));
  // ScriptableValue valueType = ScriptableValueMethods.type(Context.getCurrentContext(), textValue, new Object[] {},
  // null);
  // Assert.assertEquals("text", valueType.getValue().getValue());
  // }

  @Test
  public void testTypeForTextValue() {
    ScriptableValue textValue = newValue(TextType.get().valueOf("Text value"));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), textValue, new Object[] { }, null);
    assertEquals("text", valueType.getValue().getValue());
  }

  @Test
  public void testTypeForBooleanValue() {
    ScriptableValue booleanValue = newValue(BooleanType.get().valueOf(true));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), booleanValue, new Object[] { }, null);
    assertEquals("boolean", valueType.getValue().getValue());
  }

  @Test
  public void testTypeForDateValue() {
    ScriptableValue dateValue = newValue(DateTimeType.get().valueOf(new Date()));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), dateValue, new Object[] { }, null);
    assertEquals("datetime", valueType.getValue().getValue());
  }

  @Test
  public void testTypeForLocaleValue() {
    ScriptableValue localeValue = newValue(LocaleType.get().valueOf(Locale.CANADA_FRENCH));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), localeValue, new Object[] { }, null);
    assertEquals("locale", valueType.getValue().getValue());
  }

  @Test
  public void testTypeForBinaryValue() {
    ScriptableValue binaryContent = newValue(BinaryType.get().valueOf("binary content"));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), binaryContent, new Object[] { }, null);
    assertEquals("binary", valueType.getValue().getValue());
  }

  @Test
  public void testTypeForIntegerValue() {
    ScriptableValue integer = newValue(IntegerType.get().valueOf(1));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), integer, new Object[] { }, null);
    assertEquals("integer", valueType.getValue().getValue());
  }

  @Test
  public void convertDateToText() {
    Date currentDateTime = new Date();
    ScriptableValue dateValue = newValue(DateTimeType.get().valueOf(currentDateTime));
    ScriptableValue convertedValue = ScriptableValueMethods
        .type(getCurrentContext(), dateValue, new Object[] { "text" }, null);
    Assert.assertSame(TextType.class, convertedValue.getValueType().getClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void convertDateToInteger() {
    Date currentDateTime = new Date();
    ScriptableValue dateValue = newValue(DateTimeType.get().valueOf(currentDateTime));
    ScriptableValueMethods.type(getCurrentContext(), dateValue, new Object[] { "integer" }, null);
  }

  @Test
  public void convertBinaryToText() {
    ScriptableValue binaryContent = newValue(BinaryType.get().valueOf("binary content"));
    ScriptableValue convertedValue = ScriptableValueMethods
        .type(getCurrentContext(), binaryContent, new Object[] { "text" }, null);
    Assert.assertSame(TextType.class, convertedValue.getValueType().getClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void convertBinaryToDate() {
    ScriptableValue binaryContent = newValue(BinaryType.get().valueOf("binary content"));
    ScriptableValueMethods.type(getCurrentContext(), binaryContent, new Object[] { "datetime" }, null);
  }

  @Test
  public void convertIntegerToText() {
    ScriptableValue integerValue = newValue(IntegerType.get().valueOf(12));
    ScriptableValue convertedValue = ScriptableValueMethods
        .type(getCurrentContext(), integerValue, new Object[] { "text" }, null);
    Assert.assertSame(IntegerType.class, integerValue.getValueType().getClass());
    assertEquals("12", convertedValue.getValue().getValue());
  }

  @Test
  public void lengthOfIntegerValue() {
    ScriptableValue value = newValue(IntegerType.get().valueOf(123));
    ScriptableValue length = ScriptableValueMethods.length(getCurrentContext(), value, new Object[] { }, null);
    assertEquals(3l, length.getValue().getValue());
  }

  @Test
  public void lengthOfTextValue() {
    ScriptableValue value = newValue(TextType.get().valueOf("abcd"));
    ScriptableValue length = ScriptableValueMethods.length(getCurrentContext(), value, new Object[] { }, null);
    assertEquals(4l, length.getValue().getValue());
  }

  @Test
  public void lengthOfTextValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("abcd,efg"));
    ScriptableValue length = ScriptableValueMethods.length(getCurrentContext(), value, new Object[] { }, null);
    ValueSequence sequence = length.getValue().asSequence();
    assertEquals(2l, sequence.getSize());
    assertEquals(4l, sequence.getValues().get(0).getValue());
    assertEquals(3l, sequence.getValues().get(1).getValue());
  }

  @Test
  public void asSequence() {
    ScriptableValue value = newValue(IntegerType.get().valueOf(1));
    ScriptableValue sv = ScriptableValueMethods.asSequence(getMagmaContext(), value, null, null);
    assertThat(sv.getValue(), notNullValue());
    assertThat(sv.getValue().isNull(), is(false));
    assertThat(sv.getValue().isSequence(), is(true));

    ValueSequence sequence = sv.getValue().asSequence();
    assertThat(sequence.getSize(), is(1));
    assertThat((Long) sequence.getValues().get(0).getValue(), is(1l));
  }

  @Test
  public void asSequence_for_sequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("abcd,efg"));
    ScriptableValue sv = ScriptableValueMethods.asSequence(getMagmaContext(), value, null, null);
    assertThat(sv.getValue(), notNullValue());
    assertThat(sv.getValue().isNull(), is(false));
    assertThat(sv.getValue(), is(value.getValue()));
  }
}

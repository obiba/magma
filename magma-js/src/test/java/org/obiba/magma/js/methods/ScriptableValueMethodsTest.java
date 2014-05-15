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
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mozilla.javascript.Context.getCurrentContext;

public class ScriptableValueMethodsTest extends AbstractJsTest {

  // @Test
  // public void testValueForNull() {
  // ScriptableValue textValue = newValue(TextType.get().valueOf("Text value"));
  // ScriptableValue valueType = ScriptableValueMethods.type(Context.getCurrentContext(), textValue, new Object[] {},
  // null);
  // Assert.assertThat("text", valueType.getValue().getValue());
  // }

  @Test
  public void testTypeForTextValue() {
    ScriptableValue textValue = newValue(TextType.get().valueOf("Text value"));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), textValue, new Object[] { }, null);
    assertThat(valueType.getValue().getValue()).isEqualTo("text");
  }

  @Test
  public void testTypeForBooleanValue() {
    ScriptableValue booleanValue = newValue(BooleanType.get().valueOf(true));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), booleanValue, new Object[] { }, null);
    assertThat(valueType.getValue().getValue()).isEqualTo("boolean");
  }

  @Test
  public void testTypeForDateValue() {
    ScriptableValue dateValue = newValue(DateTimeType.get().valueOf(new Date()));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), dateValue, new Object[] { }, null);
    assertThat(valueType.getValue().getValue()).isEqualTo("datetime");
  }

  @Test
  public void testTypeForLocaleValue() {
    ScriptableValue localeValue = newValue(LocaleType.get().valueOf(Locale.CANADA_FRENCH));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), localeValue, new Object[] { }, null);
    assertThat(valueType.getValue().getValue()).isEqualTo("locale");
  }

  @Test
  public void testTypeForBinaryValue() {
    ScriptableValue binaryContent = newValue(BinaryType.get().valueOf("binary content"));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), binaryContent, new Object[] { }, null);
    assertThat(valueType.getValue().getValue()).isEqualTo("binary");
  }

  @Test
  public void testTypeForIntegerValue() {
    ScriptableValue integer = newValue(IntegerType.get().valueOf(1));
    ScriptableValue valueType = ScriptableValueMethods.type(getCurrentContext(), integer, new Object[] { }, null);
    assertThat(valueType.getValue().getValue()).isEqualTo("integer");
  }

  @Test
  public void convertDateToText() {
    Date currentDateTime = new Date();
    ScriptableValue dateValue = newValue(DateTimeType.get().valueOf(currentDateTime));
    ScriptableValue convertedValue = ScriptableValueMethods
        .type(getCurrentContext(), dateValue, new Object[] { "text" }, null);
    assertThat(convertedValue.getValueType()).isInstanceOf(TextType.class);
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
    assertThat(convertedValue.getValueType()).isInstanceOf(TextType.class);
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
    assertThat(integerValue.getValueType()).isInstanceOf(IntegerType.class);
    assertThat(convertedValue.getValue().getValue()).isEqualTo("12");
  }

  @Test
  public void lengthOfIntegerValue() {
    ScriptableValue value = newValue(IntegerType.get().valueOf(123));
    ScriptableValue length = ScriptableValueMethods.length(getCurrentContext(), value, new Object[] { }, null);
    assertThat(length.getValue().getValue()).isEqualTo(3l);
  }

  @Test
  public void lengthOfTextValue() {
    ScriptableValue value = newValue(TextType.get().valueOf("abcd"));
    ScriptableValue length = ScriptableValueMethods.length(getCurrentContext(), value, new Object[] { }, null);
    assertThat(length.getValue().getValue()).isEqualTo(4l);
  }

  @Test
  public void lengthOfTextValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("abcd,efg"));
    ScriptableValue length = ScriptableValueMethods.length(getCurrentContext(), value, new Object[] { }, null);
    ValueSequence sequence = length.getValue().asSequence();
    assertThat(sequence.getSize()).isEqualTo(2);
    assertThat(sequence.getValues().get(0).getValue()).isEqualTo(4l);
    assertThat(sequence.getValues().get(1).getValue()).isEqualTo(3l);
  }

  @Test
  public void asSequence() {
    ScriptableValue value = newValue(IntegerType.get().valueOf(1));
    ScriptableValue sv = ScriptableValueMethods.asSequence(getMagmaContext(), value, null, null);
    assertThat(sv.getValue()).isNotNull();
    assertThat(sv.getValue().isNull()).isFalse();
    assertThat(sv.getValue().isSequence()).isTrue();

    ValueSequence sequence = sv.getValue().asSequence();
    assertThat(sequence.getSize()).isEqualTo(1);
    assertThat((Long) sequence.getValues().get(0).getValue()).isEqualTo(1l);
  }

  @Test
  public void asSequence_for_sequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("abcd,efg"));
    ScriptableValue sv = ScriptableValueMethods.asSequence(getMagmaContext(), value, null, null);
    assertThat(sv.getValue()).isNotNull();
    assertThat(sv.getValue().isNull()).isFalse();
    assertThat(sv.getValue()).isEqualTo(value.getValue());
  }

  @Test
  public void testValueForBooleanValue() {
    ScriptableValue booleanValue = newValue(BooleanType.get().valueOf(true));
    Object value = ScriptableValueMethods.value(getCurrentContext(), booleanValue, new Object[] { }, null);
    assertThat(value).isInstanceOf(Boolean.class);
  }

  @Test
  public void testValueForDateTimeValue() {
    ScriptableValue dateValue = newValue(DateTimeType.get().valueOf(new Date()));
    Object value = ScriptableValueMethods.value(getCurrentContext(), dateValue, new Object[] { }, null);
    assertThat(value.getClass().getName()).isEqualTo("org.mozilla.javascript.NativeNumber");
  }

  @Test
  public void testValueForDateValue() {
    ScriptableValue dateValue = newValue(DateType.get().valueOf(new Date()));
    Object value = ScriptableValueMethods.value(getCurrentContext(), dateValue, new Object[] { }, null);
    assertThat(value.getClass().getName()).isEqualTo("org.mozilla.javascript.NativeNumber");
  }

  @Test
  public void testValueForIntegerValue() {
    ScriptableValue nbValue = newValue(IntegerType.get().valueOf(2));
    Object value = ScriptableValueMethods.value(getCurrentContext(), nbValue, new Object[] { }, null);
    assertThat(value).isInstanceOf(Double.class);
  }

  @Test
  public void testValueForDecimalValue() {
    ScriptableValue nbValue = newValue(DecimalType.get().valueOf(2));
    Object value = ScriptableValueMethods.value(getCurrentContext(), nbValue, new Object[] { }, null);
    assertThat(value).isInstanceOf(Double.class);

  }

  @Test
  public void testValueForPointValue() {
    ScriptableValue gValue = newValue(PointType.get().valueOf("[45.3,56.4]"));
    Object value = ScriptableValueMethods.value(getCurrentContext(), gValue, new Object[] { }, null);
    // array of doubles
    assertThat(value).isInstanceOf(double[].class);

  }

  @Test
  public void testValueForLineStringValue() {
    ScriptableValue gValue = newValue(LineStringType.get().valueOf("[[45.3,56.4],[55.0,46.1]]"));
    Object value = ScriptableValueMethods.value(getCurrentContext(), gValue, new Object[] { }, null);
    // array of arrays of doubles
    assertThat(value).isInstanceOf(double[][].class);
  }

  @Test
  public void testValueForPolygonValue() {
    ScriptableValue gValue = newValue(PolygonType.get().valueOf("[[[45.3,56.4],[55.0,46.1],[45.3,56.4]]]"));
    Object value = ScriptableValueMethods.value(getCurrentContext(), gValue, new Object[] { }, null);
    // array of arrays of arrays of doubles
    assertThat(value).isInstanceOf(double[][][].class);
  }

}

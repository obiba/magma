package org.obiba.magma.js.methods;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mozilla.javascript.Context.getCurrentContext;

@SuppressWarnings("ReuseOfLocalVariable")
public class TextMethodsTest extends AbstractJsTest {

  @Test
  public void testTrim() {
    ScriptableValue value = newValue(TextType.get().valueOf(" Value  "));
    ScriptableValue result = TextMethods.trim(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("Value"), result.getValue());
  }

  @Test
  public void testTrimValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\" Value1  \",\"  Value2   \""));
    ScriptableValue result = TextMethods.trim(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.getValue().asSequence().getSize());
    Assert.assertEquals(TextType.get().valueOf("Value1"), result.getValue().asSequence().get(0));
    Assert.assertEquals(TextType.get().valueOf("Value2"), result.getValue().asSequence().get(1));
  }

  @Test
  public void testMatches() {
    ScriptableValue value = newValue(TextType.get().valueOf(" Value  "));
    ScriptableValue result = TextMethods.matches(Context.getCurrentContext(), value, new Object[] { "lue" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().valueOf(true), result.getValue());
  }

  @Test
  public void testMatchesNull() {
    ScriptableValue value = newValue(TextType.get().nullValue());
    ScriptableValue result = TextMethods.matches(Context.getCurrentContext(), value, new Object[] { "lue" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().valueOf(false), result.getValue());
  }

  @Test
  public void testMatchesValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\"Value\",\"Patate\""));
    ScriptableValue result = TextMethods.matches(Context.getCurrentContext(), value, new Object[] { "lue" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.getValue().asSequence().getSize());
    Assert.assertEquals(BooleanType.get().valueOf(true), result.getValue().asSequence().get(0));
    Assert.assertEquals(BooleanType.get().valueOf(false), result.getValue().asSequence().get(1));
  }

  @Test
  public void testUpperCase() {
    ScriptableValue value = newValue(TextType.get().valueOf("value"));
    ScriptableValue result = TextMethods.upperCase(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("VALUE"), result.getValue());
  }

  @Ignore
  @Test
  public void testUpperCaseWithLocale() {
    ScriptableValue value = newValue(TextType.get().valueOf("français"));
    ScriptableValue result = TextMethods.upperCase(Context.getCurrentContext(), value, new Object[] { "fr" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("FRANÇAIS"), result.getValue());
  }

  @Test
  public void testUpperCaseValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\"Value1\",\"Value2\""));
    ScriptableValue result = TextMethods.upperCase(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.getValue().asSequence().getSize());
    Assert.assertEquals(TextType.get().valueOf("VALUE1"), result.getValue().asSequence().get(0));
    Assert.assertEquals(TextType.get().valueOf("VALUE2"), result.getValue().asSequence().get(1));
  }

  @Test
  public void testLowerCase() {
    ScriptableValue value = newValue(TextType.get().valueOf("VALUE"));
    ScriptableValue result = TextMethods.lowerCase(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("value"), result.getValue());
  }

  @Test
  public void testLowerCaseValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\"Value1\",\"Value2\""));
    ScriptableValue result = TextMethods.lowerCase(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.getValue().asSequence().getSize());
    Assert.assertEquals(TextType.get().valueOf("value1"), result.getValue().asSequence().get(0));
    Assert.assertEquals(TextType.get().valueOf("value2"), result.getValue().asSequence().get(1));
  }

  @Test
  public void testCapitalize() {
    ScriptableValue value = newValue(TextType.get().valueOf("  value foo bar"));
    ScriptableValue result = TextMethods.capitalize(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("  Value Foo Bar"), result.getValue());
  }

  @Test
  public void testCapitalizeWithDelimiters() {
    ScriptableValue value = newValue(TextType.get().valueOf("value:foo;bar_patate (toto) one"));
    ScriptableValue result = TextMethods
        .capitalize(Context.getCurrentContext(), value, new String[] { ":", ";_", "(" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("Value:Foo;Bar_Patate (Toto) one"), result.getValue());
  }

  @Test
  public void testCapitalizeValueSequence() {
    ScriptableValue value = newValue(TextType.get().sequenceOf("\"value1\",\"value2\""));
    ScriptableValue result = TextMethods.capitalize(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.getValue().asSequence().getSize());
    Assert.assertEquals(TextType.get().valueOf("Value1"), result.getValue().asSequence().get(0));
    Assert.assertEquals(TextType.get().valueOf("Value2"), result.getValue().asSequence().get(1));
  }

  @Test
  public void testReplace() {
    ScriptableValue value = newValue(TextType.get().valueOf("H2R 2E1"));
    ScriptableValue result = TextMethods.replace(Context.getCurrentContext(), value, new Object[] { " 2E1", "" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("H2R"), result.getValue());
  }

  @Test
  public void testStringConcatString() throws Exception {
    ScriptableValue hello = newValue(TextType.get().valueOf("Hello "));
    ScriptableValue world = newValue(TextType.get().valueOf("World!"));
    ScriptableValue result = TextMethods
        .concat(Context.getCurrentContext(), hello, new ScriptableValue[] { world }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("Hello World!")));
  }

  @Test
  public void testStringConcatInteger() throws Exception {
    ScriptableValue hello = newValue(TextType.get().valueOf("Hello "));
    ScriptableValue twentyThree = newValue(IntegerType.get().valueOf(23));
    ScriptableValue result = TextMethods
        .concat(Context.getCurrentContext(), hello, new ScriptableValue[] { twentyThree }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("Hello 23")));
  }

  @Test
  public void testDecimalConcatString() throws Exception {
    ScriptableValue twentyThreePointThirtyTwo = newValue(DecimalType.get().valueOf(23.32));
    ScriptableValue hello = newValue(TextType.get().valueOf(" Hello"));
    ScriptableValue result = TextMethods
        .concat(Context.getCurrentContext(), twentyThreePointThirtyTwo, new ScriptableValue[] { hello }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("23.32 Hello")));
  }

  @Test
  public void testDecimalConcatTrue() throws Exception {
    ScriptableValue twentyThreePointThirtyTwo = newValue(DecimalType.get().valueOf(23.32));
    ScriptableValue trueOperand = newValue(BooleanType.get().trueValue());
    ScriptableValue result = TextMethods
        .concat(Context.getCurrentContext(), twentyThreePointThirtyTwo, new ScriptableValue[] { trueOperand }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("23.32true")));
  }

  @Test
  public void testDecimalConcatNull() throws Exception {
    ScriptableValue twentyThreePointThirtyTwo = newValue(DecimalType.get().valueOf(23.32));
    ScriptableValue nullOperand = newValue(BooleanType.get().nullValue());
    ScriptableValue result = TextMethods
        .concat(Context.getCurrentContext(), twentyThreePointThirtyTwo, new ScriptableValue[] { nullOperand }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("23.32null")));
  }

  @Test
  public void testNullConcatString() throws Exception {
    ScriptableValue nullOperand = newValue(TextType.get().nullValue());
    ScriptableValue world = newValue(TextType.get().valueOf("World!"));
    ScriptableValue result = TextMethods
        .concat(Context.getCurrentContext(), nullOperand, new ScriptableValue[] { world }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("nullWorld!")));
  }

  @Test
  public void testConcatMultipleArguments() throws Exception {
    ScriptableValue hello = newValue(TextType.get().valueOf("Hello "));
    ScriptableValue world = newValue(TextType.get().valueOf("World!"));
    ScriptableValue greet = newValue(TextType.get().valueOf("How are you, "));
    ScriptableValue result = TextMethods
        .concat(Context.getCurrentContext(), hello, new Object[] { world, " ", greet, "Mr. Potato Head", "?" }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("Hello World! How are you, Mr. Potato Head?")));
  }

  @Test
  public void testMapWithSimpleMappingAndNormalInput() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2})", TextType.get().valueOf("YES"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf(1)));

    value = evaluate("map({'YES':1, 'NO':2})", TextType.get().valueOf("NO"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf(2)));
  }

  @Test
  public void testMapWithMappingThatHasIntegerKeyAndStringLookup() {
    ScriptableValue value = evaluate("map({'YES':1, '996':2})", TextType.get().valueOf("996"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf(2)));
  }

  @Test
  public void testMapWithMappingThatHasIntegerKeyAndIntegerLookup() {
    ScriptableValue value = evaluate("map({'YES':1, '996':2})", IntegerType.get().valueOf("996"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf(2)));
  }

  @Test
  public void testMapWithMappingThatHasIntegerKeysOnly() {
    ScriptableValue value = evaluate("map({999:1, 996:2})", IntegerType.get().valueOf("996"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf(2)));
  }

  @Test
  public void testMapWithMappingThatHasIntegerKeyAndNotFoundValue() {
    ScriptableValue value = evaluate("map({999:1, 996:2})", IntegerType.get().valueOf("998"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().nullValue()));
  }

  @Test
  public void testMapWithSimpleMappingAndNullInput() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2})", TextType.get().nullValue());
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().nullValue()));
  }

  @Test
  public void testMapAcceptsNullValueAsOutput() {
    ScriptableValue value = evaluate("map({'YES':null, 'NO':2}, 'DEFAULT')", TextType.get().valueOf("YES"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().nullValue()));
  }

  @Test
  public void testMapWithSimpleMappingAndMapNotDefinedValue() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2})", TextType.get().valueOf("NOT IN MAP"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().nullValue()));
  }

  @Test
  public void testMapWithTextMappingAndMapNotDefinedValue() {
    ScriptableValue value = evaluate("map({'YES':'1', 'NO':'2'})", TextType.get().valueOf("NOT IN MAP"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().nullValue()));
  }

  @Test
  public void testMapWithSimpleMappingAndMapNotDefinedValueWithDefaultValue() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999)", TextType.get().valueOf("NOT IN MAP"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf(9999)));
  }

  @Test
  public void testMapWithSimpleMappingAndNullValueWithNullValueMap() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999, 8888)", TextType.get().nullValue());
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf(8888)));
  }

  @Test
  public void testMapWithSimpleMappingAndNullValueWithDefaultValueMap() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999)", TextType.get().nullValue());
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf(9999)));
  }

  @Test
  public void testMapWithSimpleMappingAndNullValueWithNullSequenceMap() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999, 8888)", TextType.get().nullSequence());
    assertThat(value, notNullValue());
    assertThat(value.getValue().isNull(), is(true));
    assertThat(value.getValue().isSequence(), is(true));
    assertThat(value.getValue().asSequence(), is(TextType.get().nullSequence()));
  }

  @Test
  public void testMapWithSimpleMappingAndNullSequenceWithDefaultValueMap() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2}, 9999)", TextType.get().nullSequence());
    assertThat(value, notNullValue());
    assertThat(value.getValue().isNull(), is(true));
    assertThat(value.getValue().isSequence(), is(true));
    assertThat(value.getValue().asSequence(), is(TextType.get().nullSequence()));
  }

  @Test
  public void testMapWithSimpleMappingAndSequenceInput() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2})", TextType.get().sequenceOf("YES,NO"));
    assertThat(value, notNullValue());
    assertThat(value.getValue().isSequence(), is(true));
    assertThat(value.getValue().asSequence(), is(TextType.get().sequenceOf("1,2")));
  }

  @Test
  public void testMapWithFunctionMapping() {
    ScriptableValue value = evaluate("map({'YES':function(value){return value.concat('-YES');}, 'NO':2})",
        TextType.get().valueOf("YES"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf("YES-YES")));
  }


  @Test
  public void testDateConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf("10/23/12"));
    ScriptableValue date = TextMethods.date(getCurrentContext(), value, new Object[] { "MM/dd/yy" }, null);
    assertEquals("2012-10-23", date.getValue().toString());
  }

  @Test
  public void testEmptyDateConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf(""));
    ScriptableValue date = TextMethods.date(getCurrentContext(), value, new Object[] { "MM/dd/yy" }, null);
    assertTrue(date.getValue().isNull());
  }

  @Test
  public void testTrimEmptyDateConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf(" "));
    ScriptableValue date = TextMethods.date(getCurrentContext(), value, new Object[] { "MM/dd/yy" }, null);
    assertTrue(date.getValue().isNull());
  }

  @Test
  public void testDatetimeConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf("10/23/12 10:59 PM"));
    ScriptableValue date = TextMethods.datetime(getCurrentContext(), value, new Object[] { "MM/dd/yy h:mm a" }, null);
    String str = date.getValue().toString();
    // exclude timezone from the test
    assertEquals("2012-10-23T22:59:00.000", str.substring(0, str.lastIndexOf('-')));
  }

  @Test
  public void testEmptyDatetimeConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf(""));
    ScriptableValue date = TextMethods.datetime(getCurrentContext(), value, new Object[] { "MM/dd/yy h:mm a" }, null);
    assertTrue(date.getValue().isNull());
  }

  @Test
  public void testTrimEmptyDatetimeConvertWithFormat() {
    ScriptableValue value = newValue(TextType.get().valueOf(" "));
    ScriptableValue date = TextMethods.datetime(getCurrentContext(), value, new Object[] { "MM/dd/yy h:mm a" }, null);
    assertTrue(date.getValue().isNull());
  }


}

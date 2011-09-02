package org.obiba.magma.js.methods;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

public class TextMethodsTest extends AbstractJsTest {

  @Test
  public void testTrim() {
    ScriptableValue value = newValue(TextType.get().valueOf(" Value  "));
    ScriptableValue result = TextMethods.trim(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(TextType.get().valueOf("Value"), result.getValue());
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
    ScriptableValue result = TextMethods.concat(Context.getCurrentContext(), hello, new ScriptableValue[] { world }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("Hello World!")));
  }

  @Test
  public void testStringConcatInteger() throws Exception {
    ScriptableValue hello = newValue(TextType.get().valueOf("Hello "));
    ScriptableValue twentyThree = newValue(IntegerType.get().valueOf(23));
    ScriptableValue result = TextMethods.concat(Context.getCurrentContext(), hello, new ScriptableValue[] { twentyThree }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("Hello 23")));
  }

  @Test
  public void testDecimalConcatString() throws Exception {
    ScriptableValue twentyThreePointThirtyTwo = newValue(DecimalType.get().valueOf(23.32));
    ScriptableValue hello = newValue(TextType.get().valueOf(" Hello"));
    ScriptableValue result = TextMethods.concat(Context.getCurrentContext(), twentyThreePointThirtyTwo, new ScriptableValue[] { hello }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("23.32 Hello")));
  }

  @Test
  public void testDecimalConcatTrue() throws Exception {
    ScriptableValue twentyThreePointThirtyTwo = newValue(DecimalType.get().valueOf(23.32));
    ScriptableValue trueOperand = newValue(BooleanType.get().trueValue());
    ScriptableValue result = TextMethods.concat(Context.getCurrentContext(), twentyThreePointThirtyTwo, new ScriptableValue[] { trueOperand }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("23.32true")));
  }

  @Test
  public void testDecimalConcatNull() throws Exception {
    ScriptableValue twentyThreePointThirtyTwo = newValue(DecimalType.get().valueOf(23.32));
    ScriptableValue nullOperand = newValue(BooleanType.get().nullValue());
    ScriptableValue result = TextMethods.concat(Context.getCurrentContext(), twentyThreePointThirtyTwo, new ScriptableValue[] { nullOperand }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("23.32null")));
  }

  @Test
  public void testNullConcatString() throws Exception {
    ScriptableValue nullOperand = newValue(TextType.get().nullValue());
    ScriptableValue world = newValue(TextType.get().valueOf("World!"));
    ScriptableValue result = TextMethods.concat(Context.getCurrentContext(), nullOperand, new ScriptableValue[] { world }, null);
    assertThat(result.getValue(), is(TextType.get().valueOf("nullWorld!")));
  }

  @Test
  public void testConcatMultipleArguments() throws Exception {
    ScriptableValue hello = newValue(TextType.get().valueOf("Hello "));
    ScriptableValue world = newValue(TextType.get().valueOf("World!"));
    ScriptableValue greet = newValue(TextType.get().valueOf("How are you, "));
    ScriptableValue result = TextMethods.concat(Context.getCurrentContext(), hello, new Object[] { world, " ", greet, "Mr. Potato Head", "?" }, null);
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
  public void testMapWithSimpleMappingAndSequenceInput() {
    ScriptableValue value = evaluate("map({'YES':1, 'NO':2})", TextType.get().sequenceOf("YES,NO"));
    assertThat(value, notNullValue());
    assertThat(value.getValue().isSequence(), is(true));
    assertThat(value.getValue().asSequence(), is(TextType.get().sequenceOf("1,2")));
  }

  @Test
  public void testMapWithFunctionMapping() {
    ScriptableValue value = evaluate("map({'YES':function(value){return value.concat('-YES');}, 'NO':2})", TextType.get().valueOf("YES"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf("YES-YES")));
  }

}

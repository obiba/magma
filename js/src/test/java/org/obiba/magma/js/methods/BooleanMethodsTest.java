package org.obiba.magma.js.methods;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

public class BooleanMethodsTest extends AbstractJsTest {

  @Test
  public void testAny() {
    assertMethod("any('CAT1', 'CAT2')", TextType.get().valueOf("CAT2"), BooleanType.get().trueValue());
  }

  @Test
  public void testAnyWithNullValue() {
    assertMethod("any('CAT1', 'CAT2')", TextType.get().nullValue(), BooleanType.get().falseValue());
  }

  @Test
  public void testAll() {
    Value testValue = TextType.get().sequenceOf("\"CAT1\",\"CAT2\",\"CAT3\"");
    assertMethod("all('CAT1', 'CAT2', 'CAT3')", testValue, BooleanType.get().trueValue());
    assertMethod("all('CAT1', 'CAT2')", testValue, BooleanType.get().trueValue());
    assertMethod("all('CAT2', 'CAT3')", testValue, BooleanType.get().trueValue());
    assertMethod("all('CAT3', 'CAT4')", testValue, BooleanType.get().falseValue());
  }

  @Test
  public void testAllWithScriptableValueArguments() {
    // Create a ValueSequence containing "odd" values.
    List<Value> values = new ArrayList<Value>();
    for(int i = 1; i <= 5; i += 2) {
      values.add(TextType.get().valueOf("CAT" + i));
    }
    ScriptableValue value = newValue(ValueType.Factory.newSequence(TextType.get(), values));

    // Create a matching list of ScriptableValue arguments.
    List<ScriptableValue> args = new ArrayList<ScriptableValue>();
    for(int i = 1; i <= 5; i += 2) {
      args.add(newValue(TextType.get().valueOf("CAT" + i)));
    }

    // Verify that the ValueSequence contains all the specified "odd" values.
    ScriptableValue result = BooleanMethods.all(Context.getCurrentContext(), value, args.toArray(), null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().trueValue(), result.getValue());
  }

  @Test
  public void testAllCalledOnNullValueReturnsFalse() {
    assertMethod("all('CAT1')", TextType.get().nullValue(), BooleanType.get().falseValue());
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void test_all_CalledWithNonString() {
    assertMethod("all(1.0)", TextType.get().valueOf("some value"), BooleanType.get().falseValue());
  }

  @Test
  public void testNot() {
    assertMethod("not()", BooleanType.get().trueValue(), BooleanType.get().falseValue());
    assertMethod("not('CAT2')", TextType.get().valueOf("CAT1"), BooleanType.get().trueValue());
    assertMethod("not('CAT3')", TextType.get().sequenceOf("\"CAT1\", \"CAT2\""), BooleanType.get().trueValue());
    assertMethod("not('CAT2')", TextType.get().sequenceOf("\"CAT1\", \"CAT2\""), BooleanType.get().falseValue());
    assertMethod("not('CAT2', 'CAT3')", TextType.get().sequenceOf("\"CAT1\", \"CAT2\""), BooleanType.get().falseValue());
  }

  @Test
  public void testNotWithNullValues() {
    assertMethod("not()", BooleanType.get().nullValue(), BooleanType.get().nullValue());
    assertMethod("not()", BooleanType.get().nullSequence(), BooleanType.get().nullValue());

    assertMethod("not(null)", TextType.get().nullValue(), BooleanType.get().falseValue());
    assertMethod("not(null, null)", TextType.get().nullSequence(), BooleanType.get().falseValue());
    assertMethod("not('CAT2')", TextType.get().nullValue(), BooleanType.get().trueValue());
    assertMethod("not('CAT2', 'CAT3')", TextType.get().nullSequence(), BooleanType.get().trueValue());
  }

  @Test
  public void testTrueAndTrueReturnsTrue() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue arg = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods.and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().trueValue(), result.getValue());
  }

  @Test
  public void testTrueAndFalseReturnsFalse() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue arg = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods.and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().falseValue(), result.getValue());
  }

  @Test
  public void testTrueAndNullReturnsNull() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue arg = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods.and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().nullValue(), result.getValue());
  }

  @Test
  public void testFalseAndNullReturnsFalse() {
    ScriptableValue value = newValue(BooleanType.get().falseValue());
    ScriptableValue arg = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods.and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().falseValue(), result.getValue());
  }

  @Test
  public void testNullAndNullReturnsNull() {
    ScriptableValue value = newValue(BooleanType.get().nullValue());
    ScriptableValue arg = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods.and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().nullValue(), result.getValue());
  }

  @Test
  public void testTrueAndTrueAndFalseReturnsFalse() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue arg1 = newValue(BooleanType.get().trueValue());
    ScriptableValue arg2 = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods.and(Context.getCurrentContext(), value, new ScriptableValue[] { arg1, arg2 }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().falseValue(), result.getValue());
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void test_andOnNotABooleanThrows() {
    evaluate("and(true)", TextType.get().valueOf("This is not a boolean"));
  }

  @Test
  public void test_andAcceptsNativeJavascriptBoolean() {
    ScriptableValue value = evaluate("and(true)", BooleanType.get().trueValue());
    Assert.assertNotNull(value);
    Assert.assertEquals(BooleanType.get().trueValue(), value.getValue());
  }

  @Test
  public void testEmpty() {
    // Verify that empty() returns FALSE on a non-empty sequence.
    List<Value> values = new ArrayList<Value>();
    for(int i = 0; i < 3; i++) {
      values.add(TextType.get().valueOf("CAT" + i));
    }
    ScriptableValue nonEmptySequence = newValue(ValueType.Factory.newSequence(TextType.get(), values));
    ScriptableValue result = BooleanMethods.empty(Context.getCurrentContext(), nonEmptySequence, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().falseValue(), result.getValue());

    // Verify that empty() returns TRUE on an empty sequence.
    ScriptableValue emptySequence = newValue(ValueType.Factory.newSequence(TextType.get(), new ArrayList<Value>()));
    result = BooleanMethods.empty(Context.getCurrentContext(), emptySequence, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().trueValue(), result.getValue());
  }

  // Ternary valued OR

  @Test
  public void testTrueOrTrueEqualsTrue() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue trueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods.or(Context.getCurrentContext(), trueOne, new ScriptableValue[] { trueTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testTrueOrFalseEqualsTrue() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue falseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods.or(Context.getCurrentContext(), trueOne, new ScriptableValue[] { falseTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testFalseOrTrueEqualsTrue() throws Exception {
    ScriptableValue falseOne = newValue(BooleanType.get().falseValue());
    ScriptableValue trueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods.or(Context.getCurrentContext(), falseOne, new ScriptableValue[] { trueTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testFalseOrFalseEqualsFalse() throws Exception {
    ScriptableValue falseOne = newValue(BooleanType.get().falseValue());
    ScriptableValue falseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods.or(Context.getCurrentContext(), falseOne, new ScriptableValue[] { falseTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));
  }

  @Test
  public void testTrueOrUnknownEqualsTrue() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue nullTwo = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods.or(Context.getCurrentContext(), trueOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testUnknownOrTrueEqualsTrue() throws Exception {
    ScriptableValue nullOne = newValue(BooleanType.get().nullValue());
    ScriptableValue trueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods.or(Context.getCurrentContext(), nullOne, new ScriptableValue[] { trueTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testUnknownOrUnknownEqualsUnknown() throws Exception {
    ScriptableValue nullOne = newValue(BooleanType.get().nullValue());
    ScriptableValue nullTwo = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods.or(Context.getCurrentContext(), nullOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void testFalseOrUnknownEqualsUnknown() throws Exception {
    ScriptableValue falseOne = newValue(BooleanType.get().falseValue());
    ScriptableValue nullTwo = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods.or(Context.getCurrentContext(), falseOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void testUnknownOrFalseEqualsUnknown() throws Exception {
    ScriptableValue nullOne = newValue(BooleanType.get().nullValue());
    ScriptableValue falseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods.or(Context.getCurrentContext(), nullOne, new ScriptableValue[] { falseTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void test_orOnNotABooleanThrows() {
    evaluate("or(true)", TextType.get().valueOf("This is not a boolean"));
  }

  @Test
  public void test_orAcceptsNativeJavascriptBoolean() {
    ScriptableValue value = evaluate("or(true)", BooleanType.get().trueValue());
    Assert.assertNotNull(value);
    Assert.assertEquals(BooleanType.get().trueValue(), value.getValue());
  }

  // equals (==)

  @Test
  public void testIntegerTwoEqualsIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integer2 = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods.eq(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integer2 }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testDecimalTwoEqualsIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods.eq(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testTrueEqualsTrue() throws Exception {
    ScriptableValue booleanTrueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue booleanTrueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods.eq(Context.getCurrentContext(), booleanTrueOne, new ScriptableValue[] { booleanTrueTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testTrueEqualsFalse() throws Exception {
    ScriptableValue booleanTrueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue booleanFalseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods.eq(Context.getCurrentContext(), booleanTrueOne, new ScriptableValue[] { booleanFalseTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));
  }

  @Test
  public void testTextFooEqualsTextBarEqualsFalse() throws Exception {
    ScriptableValue foo = newValue(TextType.get().valueOf("foo"));
    ScriptableValue bar = newValue(TextType.get().valueOf("bar"));
    ScriptableValue result = BooleanMethods.eq(Context.getCurrentContext(), foo, new ScriptableValue[] { bar }, null);
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));
  }

  @Test
  public void testTextFooEqualsTextFooEqualsTrue() throws Exception {
    ScriptableValue fooOne = newValue(TextType.get().valueOf("foo"));
    ScriptableValue fooTwo = newValue(TextType.get().valueOf("foo"));
    ScriptableValue result = BooleanMethods.eq(Context.getCurrentContext(), fooOne, new ScriptableValue[] { fooTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  // isNull, isNotNull
  @Test
  public void test_isNull() {
    assertMethod("isNull()", TextType.get().nullValue(), BooleanType.get().trueValue());
    assertMethod("isNull()", TextType.get().valueOf("Not Null"), BooleanType.get().falseValue());

    assertMethod("isNull()", TextType.get().nullSequence(), BooleanType.get().trueValue());
    assertMethod("isNull()", TextType.get().sequenceOf("Not Null"), BooleanType.get().falseValue());
  }

  @Test
  public void test_isNotNull() {
    assertMethod("isNotNull()", TextType.get().nullValue(), BooleanType.get().falseValue());
    assertMethod("isNotNull()", TextType.get().valueOf("Not Null"), BooleanType.get().trueValue());

    assertMethod("isNotNull()", TextType.get().nullSequence(), BooleanType.get().falseValue());
    assertMethod("isNotNull()", TextType.get().sequenceOf("Not Null"), BooleanType.get().trueValue());
  }

  private void assertMethod(String script, Value value, Value expected) {
    ScriptableValue result = evaluate(script, value);
    assertThat(result, notNullValue());
    assertThat(result.getValue(), notNullValue());
    assertThat(result.getValue(), is(expected));
  }

  // whenNull
  @Test
  public void test_whenNull() {
    ScriptableValue result = evaluate("whenNull(false)", BooleanType.get().valueOf((Object) null));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(false)));

    result = evaluate("whenNull(true)", BooleanType.get().valueOf((Object) null));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(true)));

    result = evaluate("whenNull(null)", BooleanType.get().valueOf((Object) null));
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));

    result = evaluate("whenNull()", BooleanType.get().valueOf((Object) null));
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));

    result = evaluate("whenNull(false)", BooleanType.get().valueOf(true));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(true)));

    result = evaluate("whenNull(true)", BooleanType.get().valueOf(true));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(true)));

    result = evaluate("whenNull(null)", BooleanType.get().valueOf(true));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(true)));

    result = evaluate("whenNull()", BooleanType.get().valueOf(true));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(true)));

    result = evaluate("whenNull(false)", BooleanType.get().valueOf(false));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(false)));

    result = evaluate("whenNull(true)", BooleanType.get().valueOf(false));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(false)));

    result = evaluate("whenNull(null)", BooleanType.get().valueOf(false));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(false)));

    result = evaluate("whenNull()", BooleanType.get().valueOf(false));
    assertThat(result.getValue(), is(BooleanType.get().valueOf(false)));

  }

}

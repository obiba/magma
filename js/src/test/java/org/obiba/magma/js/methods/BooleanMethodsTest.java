package org.obiba.magma.js.methods;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.AbstractScriptableValueTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

public class BooleanMethodsTest extends AbstractScriptableValueTest {

  @Test
  public void testAny() {
    ScriptableValue value = newValue(TextType.get().valueOf("CAT2"));
    ScriptableValue result = BooleanMethods.any(Context.getCurrentContext(), value, new String[] { "CAT1", "CAT2" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().trueValue(), result.getValue());
  }

  @Test
  public void testAll() {
    // Create a ValueSequence containing "odd" values.
    List<Value> values = new ArrayList<Value>();
    for(int i = 1; i <= 5; i += 2) {
      values.add(TextType.get().valueOf("CAT" + i));
    }
    ScriptableValue value = newValue(ValueType.Factory.newSequence(TextType.get(), values));

    // Verify that the ValueSequence does NOT contain all the specified "even" values.
    ScriptableValue result = BooleanMethods.all(Context.getCurrentContext(), value, new String[] { "CAT2", "CAT4", "CAT6" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().falseValue(), result.getValue());

    // Verify that the ValueSequence contains all the specified "odd" values.
    result = BooleanMethods.all(Context.getCurrentContext(), value, new String[] { "CAT1", "CAT3", "CAT5" }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().trueValue(), result.getValue());
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
    ScriptableValue nullValue = newValue(TextType.get().nullValue());
    ScriptableValue result = BooleanMethods.all(Context.getCurrentContext(), nullValue, new String[] { "CAT1", "CAT3", "CAT5" }, null);
    Assert.assertNotNull(result);
    Assert.assertFalse(result.getValue().isNull());
    Assert.assertEquals(BooleanType.get().falseValue(), result.getValue());
  }

  @Test
  public void testAnyWithNullValue() {
    ScriptableValue value = newValue(TextType.get().valueOf(null));
    ScriptableValue result = BooleanMethods.any(Context.getCurrentContext(), value, new String[] { "CAT1", "CAT2" }, null);
    Assert.assertNotNull(result);
    Assert.assertFalse(result.getValue().isNull());
    Assert.assertEquals(BooleanType.get().falseValue(), result.getValue());
  }

  @Test
  public void testNot() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods.not(Context.getCurrentContext(), value, null, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().falseValue(), result.getValue());
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

  // gt (>)

  @Test
  public void testIntegerTwoGtIntegerThreeEqualsFalse() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integerThree = newValue(IntegerType.get().valueOf(3));
    ScriptableValue result = BooleanMethods.gt(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integerThree }, null);
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));
  }

  @Test
  public void testDecimalTwoGtIntegerOneEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = BooleanMethods.gt(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerOne }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  // ge (>=)

  @Test
  public void testIntegerTwoGeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integer2 = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods.ge(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integer2 }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testDecimalTwoGeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods.ge(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  // lt (<)

  @Test
  public void testIntegerTwoLtIntegerThreeEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integerThree = newValue(IntegerType.get().valueOf(3));
    ScriptableValue result = BooleanMethods.lt(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integerThree }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testDecimalTwoLtIntegerOneEqualsFalse() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = BooleanMethods.lt(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerOne }, null);
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));
  }

  // le (<=)

  @Test
  public void testIntegerTwoLeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integer2 = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods.ge(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integer2 }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void testDecimalTwoLeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods.ge(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }
}

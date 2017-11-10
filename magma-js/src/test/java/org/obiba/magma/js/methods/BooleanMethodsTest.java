/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.methods;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings({ "ReuseOfLocalVariable", "OverlyLongMethod", "PMD.NcssMethodCount" })
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
  public void texAnyNoArgsFalse() {
    assertMethod("any()", TextType.get().valueOf("CAT2"), BooleanType.get().falseValue());
  }

  @Test
  public void testAnyFunctionOnSequence() {
    assertMethod("any(function(v) { return v.ge(3) })", IntegerType.get().sequenceOf("1,2,3,4"), BooleanType.get().trueValue());
    assertMethod("any(function(v) { return v.ge(30) })", IntegerType.get().sequenceOf("1,2,3,4"), BooleanType.get().falseValue());
  }

  @Test
  public void testAnyFunctionOnSequenceWithNull() {
    assertMethod("any(function(v) { return v.ge(3) })", IntegerType.get().sequenceOf(Lists.newArrayList(
        IntegerType.get().valueOf(1), IntegerType.get().nullValue(),
        IntegerType.get().valueOf(2), IntegerType.get().nullValue(),
        IntegerType.get().valueOf(3), IntegerType.get().nullValue())), BooleanType.get().trueValue());
  }

  @Test
  public void testAnyFunctionOnNullSequence() {
    assertMethod("any(function(v) { return v.ge(3) })", IntegerType.get().nullSequence(), BooleanType.get().falseValue());
  }

  @Test
  public void testAnyFunctionOnSingleValue() {
    assertMethod("any(function(v) { return v.ge(3) })", IntegerType.get().valueOf("4"), BooleanType.get().trueValue());
    assertMethod("any(function(v) { return v.ge(30) })", IntegerType.get().valueOf("4"), BooleanType.get().falseValue());
  }

  @Test
  public void testAnyFunctionOnNullSingleValue() {
    assertMethod("any(function(v) { return v.ge(3) })", IntegerType.get().nullValue(), BooleanType.get().falseValue());
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
    Collection<Value> values = new ArrayList<>();
    for(int i = 1; i <= 5; i += 2) {
      values.add(TextType.get().valueOf("CAT" + i));
    }
    ScriptableValue value = newValue(ValueType.Factory.newSequence(TextType.get(), values));

    // Create a matching list of ScriptableValue arguments.
    Collection<ScriptableValue> args = new ArrayList<>();
    for(int i = 1; i <= 5; i += 2) {
      args.add(newValue(TextType.get().valueOf("CAT" + i)));
    }

    // Verify that the ValueSequence contains all the specified "odd" values.
    ScriptableValue result = BooleanMethods.all(Context.getCurrentContext(), value, args.toArray(), null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testAllCalledOnNullValueReturnsFalse() {
    assertMethod("all('CAT1')", TextType.get().nullValue(), BooleanType.get().falseValue());
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void test_all_CalledWithNonString() {
    assertMethod("all(1.0)", TextType.get().valueOf("some value"), BooleanType.get().falseValue());
  }

  // Not

  @Test
  public void testNot() {
    assertMethod("not()", BooleanType.get().trueValue(), BooleanType.get().falseValue());
    assertMethod("not('CAT2')", TextType.get().valueOf("CAT1"), BooleanType.get().trueValue());
    assertMethod("not('CAT2')", TextType.get().valueOf("CAT2"), BooleanType.get().falseValue());
    assertMethod("not('CAT3')", TextType.get().sequenceOf("\"CAT1\", \"CAT2\""), BooleanType.get().trueValue());
    assertMethod("not('CAT2')", TextType.get().sequenceOf("\"CAT1\", \"CAT2\""), BooleanType.get().falseValue());
    assertMethod("not('CAT2', 'CAT3')", TextType.get().sequenceOf("\"CAT1\", \"CAT2\""),
        BooleanType.get().falseValue());
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void test_nullNotBooleanType() {
    ScriptableValue value = newValue(TextType.get().valueOf("string"));
    assertMethod("not()", value.getValue(), null);
  }

  @Test
  public void testNotWithBooleanSequence() {
    Collection<Value> trueList = new ArrayList<>();
    trueList.add(BooleanType.get().trueValue());
    ScriptableValue result = evaluate("not()", BooleanType.get().sequenceOf(trueList));
    assertThat(result).isNotNull();
    assertThat(result.getValue().isSequence()).isTrue();
    assertThat(result.getValue().asSequence().getSize()).isEqualTo(1);
    assertThat(result.getValue().asSequence().get(0)).isEqualTo(BooleanType.get().falseValue());
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

  // And
  @Test
  public void testTrueAndTrueReturnsTrue() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue arg = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods
        .and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testTrueAndFalseReturnsFalse() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue arg = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods
        .and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void testTrueAndNullReturnsNull() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue arg = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods
        .and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().nullValue());
  }

  @Test
  public void testFalseAndNullReturnsFalse() {
    ScriptableValue value = newValue(BooleanType.get().falseValue());
    ScriptableValue arg = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods
        .and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void testNullAndNullReturnsNull() {
    ScriptableValue value = newValue(BooleanType.get().nullValue());
    ScriptableValue arg = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods
        .and(Context.getCurrentContext(), value, new ScriptableValue[] { arg }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().nullValue());
  }

  @Test
  public void testTrueAndTrueAndFalseReturnsFalse() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue arg1 = newValue(BooleanType.get().trueValue());
    ScriptableValue arg2 = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods
        .and(Context.getCurrentContext(), value, new ScriptableValue[] { arg1, arg2 }, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void test_andOnNotABooleanThrows() {
    ScriptableValue value = evaluate("and(true)", TextType.get().valueOf("This is not a boolean"));
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_andAcceptsNativeJavascriptBoolean() {
    ScriptableValue value = evaluate("and(true)", BooleanType.get().trueValue());
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_andNullARGReturnsTrue() {
    ScriptableValue value = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods.and(Context.getCurrentContext(), value, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  // Empty
  @Test
  public void testEmpty() {
    // Verify that empty() returns FALSE on a non-empty sequence.
    Collection<Value> values = new ArrayList<>();
    for(int i = 0; i < 3; i++) {
      values.add(TextType.get().valueOf("CAT" + i));
    }
    ScriptableValue nonEmptySequence = newValue(ValueType.Factory.newSequence(TextType.get(), values));
    ScriptableValue result = BooleanMethods.empty(Context.getCurrentContext(), nonEmptySequence, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());

    // Verify that empty() returns TRUE on an empty sequence.
    ScriptableValue emptySequence = newValue(ValueType.Factory.newSequence(TextType.get(), new ArrayList<Value>()));
    result = BooleanMethods.empty(Context.getCurrentContext(), emptySequence, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());

    emptySequence = newValue(TextType.get().nullSequence());
    result = BooleanMethods.empty(Context.getCurrentContext(), emptySequence, null, null);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(BooleanType.get().nullValue());
  }

  // Ternary valued OR

  @Test
  public void testTrueOrTrueEqualsTrue() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue trueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods
        .or(Context.getCurrentContext(), trueOne, new ScriptableValue[] { trueTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testTrueOrFalseEqualsTrue() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue falseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods
        .or(Context.getCurrentContext(), trueOne, new ScriptableValue[] { falseTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testFalseOrTrueEqualsTrue() throws Exception {
    ScriptableValue falseOne = newValue(BooleanType.get().falseValue());
    ScriptableValue trueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods
        .or(Context.getCurrentContext(), falseOne, new ScriptableValue[] { trueTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testFalseOrFalseEqualsFalse() throws Exception {
    ScriptableValue falseOne = newValue(BooleanType.get().falseValue());
    ScriptableValue falseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods
        .or(Context.getCurrentContext(), falseOne, new ScriptableValue[] { falseTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void testTrueOrUnknownEqualsTrue() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue nullTwo = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods
        .or(Context.getCurrentContext(), trueOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testUnknownOrTrueEqualsTrue() throws Exception {
    ScriptableValue nullOne = newValue(BooleanType.get().nullValue());
    ScriptableValue trueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods
        .or(Context.getCurrentContext(), nullOne, new ScriptableValue[] { trueTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testUnknownOrUnknownEqualsUnknown() throws Exception {
    ScriptableValue nullOne = newValue(BooleanType.get().nullValue());
    ScriptableValue nullTwo = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods
        .or(Context.getCurrentContext(), nullOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().nullValue());
  }

  @Test
  public void testFalseOrUnknownEqualsUnknown() throws Exception {
    ScriptableValue falseOne = newValue(BooleanType.get().falseValue());
    ScriptableValue nullTwo = newValue(BooleanType.get().nullValue());
    ScriptableValue result = BooleanMethods
        .or(Context.getCurrentContext(), falseOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().nullValue());
  }

  @Test
  public void testUnknownOrFalseEqualsUnknown() throws Exception {
    ScriptableValue nullOne = newValue(BooleanType.get().nullValue());
    ScriptableValue falseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods
        .or(Context.getCurrentContext(), nullOne, new ScriptableValue[] { falseTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().nullValue());
  }

  @Test
  public void test_orAcceptsNativeJavascriptBoolean() {
    ScriptableValue value = evaluate("or(true)", BooleanType.get().trueValue());
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_orMultipleBoolean() {
    ScriptableValue value = evaluate("or(false,true)", BooleanType.get().falseValue());
    assertThat(value).isNotNull();
    assertThat(value.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  // equals (==)

  @Test
  public void testIntegerTwoEqualsIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integer2 = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integer2 }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testIntegerTwoEqualsDecimalTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { decimalTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testIntegerFourEqualsDecimalTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(4.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { decimalTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void testDecimalTwoEqualsIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testDecimalFourEqualsIntegerTwoEqualsFalse() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(4.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void testDecimalFourEqualsDecimalTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { decimalTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testDecimalFourEqualsDecimalTwoPointTwoEqualsFalse() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue decimalTwoPointTwo = newValue(DecimalType.get().valueOf(2.2));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { decimalTwoPointTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void test_eq_NumberValueEqNotScriptableValueEqualsTrue() throws Exception {
    test_eq_NumberValueEqNotScriptableValueEqualsTrue(IntegerType.get().valueOf(2));
    test_eq_NumberValueEqNotScriptableValueEqualsTrue(DecimalType.get().valueOf(2));
  }

  private void test_eq_NumberValueEqNotScriptableValueEqualsTrue(Value number) throws Exception {
    ScriptableValue result = evaluate("eq(2)", number);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
    result = evaluate("eq(2.0)", number);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
    result = evaluate("eq('2')", number);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_eq_TextValueEqNotScriptableValueEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq('patate')", TextType.get().valueOf("patate"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_eq_TextValueEqNotScriptableValueSequenceEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq('patate','pwel')", TextType.get().valueOf("patate"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void test_eq_TextValueSequenceEqNotScriptableValueEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq('patate','pwel')", TextType.get().sequenceOf("patate,pwel"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_eq_TextValueSequenceWithNullEqNotScriptableValueEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq('patate',null,'pwel')", TextType.get().sequenceOf("patate,,pwel"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_eq_TextNullValueEqNotScriptableValueEqualsFalse() throws Exception {
    ScriptableValue result = evaluate("eq('patate')", TextType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void test_eq_TextNullValueEqNullValueEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq(null)", TextType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_eq_DateValueEqNotScriptableValueEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq('2011-01-29')", DateType.get().valueOf("2011-01-29"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_eq_IntegerValueEqNotScriptableValueEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq(1)", IntegerType.get().valueOf("1"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_eq_IntegerValueSequenceEqNotScriptableValueEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq(1,2,3)", IntegerType.get().sequenceOf("1,2,3"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_eq_OneIntegerValueSequenceEqNotScriptableValueEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq(1)", IntegerType.get().sequenceOf("1"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void test_eq_DecimalValueEqNotScriptableValueEqualsTrue() throws Exception {
    ScriptableValue result = evaluate("eq(1)", DecimalType.get().valueOf("1.0"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
    result = evaluate("eq(1.1)", DecimalType.get().valueOf("1.1"));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testTrueEqualsTrue() throws Exception {
    ScriptableValue booleanTrueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue booleanTrueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), booleanTrueOne, new ScriptableValue[] { booleanTrueTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testTrueEqualsFalse() throws Exception {
    ScriptableValue booleanTrueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue booleanFalseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), booleanTrueOne, new ScriptableValue[] { booleanFalseTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void testTextFooEqualsTextBarEqualsFalse() throws Exception {
    ScriptableValue foo = newValue(TextType.get().valueOf("foo"));
    ScriptableValue bar = newValue(TextType.get().valueOf("bar"));
    ScriptableValue result = BooleanMethods.eq(Context.getCurrentContext(), foo, new ScriptableValue[] { bar }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void testTextFooEqualsTextFooEqualsTrue() throws Exception {
    ScriptableValue fooOne = newValue(TextType.get().valueOf("foo"));
    ScriptableValue fooTwo = newValue(TextType.get().valueOf("foo"));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), fooOne, new ScriptableValue[] { fooTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testSequenceEqualsValue() throws Exception {
    ScriptableValue fooOne = newValue(TextType.get().sequenceOf("foo,foo"));
    ScriptableValue fooTwo = newValue(TextType.get().valueOf("foo"));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), fooOne, new ScriptableValue[] { fooTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void testSequenceEqualsSequence() throws Exception {
    ScriptableValue fooOne = newValue(TextType.get().sequenceOf("foo,bar"));
    ScriptableValue fooTwo = newValue(TextType.get().sequenceOf("foo,bar"));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), fooOne, new ScriptableValue[] { fooTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testSequenceNotEqualsSequence() throws Exception {
    ScriptableValue fooOne = newValue(TextType.get().sequenceOf("foo,foo"));
    ScriptableValue fooTwo = newValue(TextType.get().sequenceOf("foo"));
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), fooOne, new ScriptableValue[] { fooTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void testEmptySequenceEqualsEmptySequence() throws Exception {
    ScriptableValue fooOne = newValue(TextType.get().nullSequence());
    ScriptableValue fooTwo = newValue(TextType.get().nullSequence());
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), fooOne, new ScriptableValue[] { fooTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testEmptySequenceEqualsEmptyValue() throws Exception {
    ScriptableValue fooOne = newValue(TextType.get().nullSequence());
    ScriptableValue fooTwo = newValue(TextType.get().nullValue());
    ScriptableValue result = BooleanMethods
        .eq(Context.getCurrentContext(), fooOne, new ScriptableValue[] { fooTwo }, null);
    assertThat(result.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testDateTimeEqualsDateTimeEqualsTrue() {
    ScriptableValue dateTime = newValue(DateTimeType.get().now());
    ScriptableValue resultDateTime = BooleanMethods
        .eq(Context.getCurrentContext(), dateTime, new Object[] { dateTime }, null);
    assertThat(resultDateTime.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testDateEqualsDateEqualsTrue() {
    ScriptableValue date = newValue(DateType.get().now());
    ScriptableValue resultDate = BooleanMethods.eq(Context.getCurrentContext(), date, new Object[] { date }, null);
    assertThat(resultDate.getValue()).isEqualTo(BooleanType.get().trueValue());
  }

  @Test
  public void testEqualsArgsNULLFirstOperand() {
    ScriptableValue date = newValue(DateType.get().now());
    ScriptableValue resultDate = BooleanMethods.eq(Context.getCurrentContext(), date, null, null);
    assertThat(resultDate.getValue()).isEqualTo(BooleanType.get().falseValue());
  }

  @Test
  public void textEqualsMockDataType() {
    ScriptableValue text = newValue(TextType.get().valueOf("foo"));
    ScriptableValue date = newValue(DateType.get().now());
    ScriptableValue resultDate = BooleanMethods.eq(Context.getCurrentContext(), date, new Object[] { text }, null);
    assertThat(resultDate.getValue()).isEqualTo(BooleanType.get().falseValue());
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

  // whenNull
  @Test
  public void test_whenNullBoolean() {
    ScriptableValue result = evaluate("whenNull(false)", BooleanType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(false));

    result = evaluate("whenNull(true)", BooleanType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(true));

    result = evaluate("whenNull(null)", BooleanType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(BooleanType.get().nullValue());

    result = evaluate("whenNull()", BooleanType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(BooleanType.get().nullValue());

    result = evaluate("whenNull(false)", BooleanType.get().valueOf(true));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(true));

    result = evaluate("whenNull(true)", BooleanType.get().valueOf(true));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(true));

    result = evaluate("whenNull(null)", BooleanType.get().valueOf(true));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(true));

    result = evaluate("whenNull()", BooleanType.get().valueOf(true));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(true));

    result = evaluate("whenNull(false)", BooleanType.get().valueOf(false));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(false));

    result = evaluate("whenNull(true)", BooleanType.get().valueOf(false));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(false));

    result = evaluate("whenNull(null)", BooleanType.get().valueOf(false));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(false));

    result = evaluate("whenNull()", BooleanType.get().valueOf(false));
    assertThat(result.getValue()).isEqualTo(BooleanType.get().valueOf(false));
  }

  @Test
  public void test_whenNullText() {
    ScriptableValue result = evaluate("whenNull('patate')", TextType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("patate"));

    result = evaluate("whenNull(newValue('patate'))", TextType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("patate"));

    result = evaluate("whenNull(newValue(1))", TextType.get().nullValue());
    assertThat(result.getValue().getValueType()).isEqualTo(DecimalType.get());

    result = evaluate("whenNull(1)", TextType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(TextType.get().valueOf("1.0"));
  }

  @Test
  public void test_whenNullInteger() {
    ScriptableValue result = evaluate("whenNull(1)", IntegerType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));

    result = evaluate("whenNull(newValue(1))", IntegerType.get().nullValue());
    assertThat(result.getValue().getValueType()).isEqualTo(DecimalType.get());

    result = evaluate("whenNull('1')", IntegerType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));

    result = evaluate("whenNull()", IntegerType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(IntegerType.get().nullValue());
  }

  @Test
  public void test_whenNullDecimal() {
    ScriptableValue result = evaluate("whenNull(1)", DecimalType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(DecimalType.get().valueOf(1));

    result = evaluate("whenNull(newValue(1))", DecimalType.get().nullValue());
    assertThat(result.getValue().getValueType()).isEqualTo(DecimalType.get());

    result = evaluate("whenNull('1')", DecimalType.get().nullValue());
    assertThat(result.getValue()).isEqualTo(DecimalType.get().valueOf(1));
  }

  @Test
  public void test_whenNullTextSequence() {
    Collection<Value> values = new ArrayList<>();
    values.add(TextType.get().valueOf("pwel"));
    values.add(TextType.get().nullValue());

    ScriptableValue result = evaluate("whenNull('patate')", TextType.get().sequenceOf(values));
    assertThat(result.getValue().isSequence()).isTrue();
    assertThat(result.getValue().asSequence().getSize()).isEqualTo(2);
    assertThat(result.getValue().asSequence().get(0)).isEqualTo(TextType.get().valueOf("pwel"));
    assertThat(result.getValue().asSequence().get(1)).isEqualTo(TextType.get().valueOf("patate"));

    result = evaluate("whenNull('patate')", TextType.get().nullSequence());
    assertThat(result.getValue().isSequence()).isTrue();
    assertThat(result.getValue().asSequence().getSize()).isEqualTo(1);
    assertThat(result.getValue().asSequence().get(0)).isEqualTo(TextType.get().valueOf("patate"));
  }

}

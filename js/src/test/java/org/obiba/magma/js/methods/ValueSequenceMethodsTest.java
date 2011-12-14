package org.obiba.magma.js.methods;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.support.Values;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Iterables;

public class ValueSequenceMethodsTest extends AbstractJsTest {

  // first()

  @Test
  public void testFirstOfOne() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("A");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(valueSequence.get(0)));
  }

  @Test
  public void testFirstOfTwo() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(valueSequence.get(0)));
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void testFirstOnNonSequence() throws Exception {
    Value value = TextType.get().valueOf("A-Value");
    ScriptableValue scriptableValue = newValue(value);
    ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
  }

  @Test
  public void testFirstOnNullOperand() throws Exception {
    ScriptableValue scriptableValue = newValue(TextType.get().nullValue());
    ScriptableValue result = ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(scriptableValue.getValue()));
  }

  @Test
  public void testFirstOfNone() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf(new ArrayList<Value>());
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(TextType.get().nullValue()));
  }

  // last()

  @Test
  public void testLastOfOne() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("A");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(valueSequence.get(0)));
  }

  @Test
  public void testLastOfTwo() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(valueSequence.get(1)));
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void testLastOnNonSequence() throws Exception {
    Value value = TextType.get().valueOf("A-Value");
    ScriptableValue scriptableValue = newValue(value);
    ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
  }

  @Test
  public void testLastOnNullOperand() throws Exception {
    ScriptableValue scriptableValue = newValue(TextType.get().nullValue());
    ScriptableValue result = ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(scriptableValue.getValue()));
  }

  @Test
  public void testLastOfNone() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf(new ArrayList<Value>());
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(TextType.get().nullValue()));
  }

  // size()

  @Test
  public void testSizeOfOne() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("A");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(valueSequence.getSize())));
  }

  @Test
  public void testSizeOfTwo() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(valueSequence.getSize())));
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void testSizeOnNonSequence() throws Exception {
    Value value = TextType.get().valueOf("A-Value");
    ScriptableValue scriptableValue = newValue(value);
    ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
  }

  @Test
  public void testSizeOnNullOperand() throws Exception {
    ScriptableValue scriptableValue = newValue(TextType.get().nullValue());
    ScriptableValue result = ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void testSizeOfNone() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf(new ArrayList<Value>());
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(valueSequence.getSize())));
  }

  // value(int index)

  @Test
  public void testValueFirstItemExists() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { 0 }, null);
    assertThat(result.getValue(), is(valueSequence.get(0)));
  }

  @Test
  public void testValueSecondItemExists() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { 1 }, null);
    assertThat(result.getValue(), is(valueSequence.get(1)));
  }

  @Test
  public void testValueIndexOutOfBounds() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { 2 }, null);
    assertThat(result.getValue(), is(TextType.get().nullValue()));
  }

  @Test
  public void testValueIndexNonIntegerType() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { "One" }, null);
    assertThat(result.getValue(), is(TextType.get().nullValue()));
  }

  // sort()

  @Test
  public void testSortTextNaturalOrder() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"D\", \"C\", \"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    valueSequence = ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, null, null).getValue().asSequence();
    assertThat(valueSequence.getValues().get(0), is(TextType.get().valueOf("A")));
    assertThat(valueSequence.getValues().get(1), is(TextType.get().valueOf("B")));
    assertThat(valueSequence.getValues().get(2), is(TextType.get().valueOf("C")));
    assertThat(valueSequence.getValues().get(3), is(TextType.get().valueOf("D")));
  }

  @Test
  public void testSortIntegerNaturalOrder() throws Exception {
    ValueSequence valueSequence = IntegerType.get().sequenceOf("4,3,1,2");
    ScriptableValue scriptableValue = newValue(valueSequence);
    valueSequence = ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, null, null).getValue().asSequence();
    assertThat(valueSequence.getValues().get(0), is(IntegerType.get().valueOf(1)));
    assertThat(valueSequence.getValues().get(1), is(IntegerType.get().valueOf(2)));
    assertThat(valueSequence.getValues().get(2), is(IntegerType.get().valueOf(3)));
    assertThat(valueSequence.getValues().get(3), is(IntegerType.get().valueOf(4)));
  }

  @Test
  public void testSortIntegerDecendingUsingSortFunction() throws Exception {
    ValueSequence valueSequence = IntegerType.get().sequenceOf("4,3,1,2");

    MyScriptableValueCustomSortDesc scriptableValue = newValueDesc(valueSequence);
    FunctionObject funObj = new FunctionObject("sort", scriptableValue.getClass().getMethod("sort", new Class[] { ScriptableValue.class, ScriptableValue.class }), scriptableValue);
    valueSequence = ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, new Object[] { funObj }, null).getValue().asSequence();
    assertThat(valueSequence.getValues().get(0), is(IntegerType.get().valueOf(4)));
    assertThat(valueSequence.getValues().get(1), is(IntegerType.get().valueOf(3)));
    assertThat(valueSequence.getValues().get(2), is(IntegerType.get().valueOf(2)));
    assertThat(valueSequence.getValues().get(3), is(IntegerType.get().valueOf(1)));
  }

  @Test
  public void testSortIntegerAscendingUsingSortFunction() throws Exception {
    ValueSequence valueSequence = IntegerType.get().sequenceOf("4,3,1,2");

    MyScriptableValueCustomSortAsc scriptableValue = newValueAsc(valueSequence);
    FunctionObject funObj = new FunctionObject("sort", scriptableValue.getClass().getMethod("sort", new Class[] { ScriptableValue.class, ScriptableValue.class }), scriptableValue);
    valueSequence = ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, new Object[] { funObj }, null).getValue().asSequence();
    assertThat(valueSequence.getValues().get(0), is(IntegerType.get().valueOf(1)));
    assertThat(valueSequence.getValues().get(1), is(IntegerType.get().valueOf(2)));
    assertThat(valueSequence.getValues().get(2), is(IntegerType.get().valueOf(3)));
    assertThat(valueSequence.getValues().get(3), is(IntegerType.get().valueOf(4)));
  }

  @Test
  public void test_sort_nullSequenceReturnsNullSequence() {
    assertMethod("sort()", IntegerType.get().nullSequence(), IntegerType.get().nullSequence());
  }

  @Test
  public void test_sort_nonSequenceReturnsSingleValue() {
    assertMethod("sort()", IntegerType.get().valueOf(4), IntegerType.get().valueOf(4));
  }

  // avg

  @Test
  public void test_avg_integerType() {
    assertAvgIs(Values.asSequence(IntegerType.get(), 1, 2, 3, 4), 2.5);
  }

  @Test
  public void test_avg_decimalType() {
    assertAvgIs(Values.asSequence(DecimalType.get(), 1, 2, 3, 4), 2.5);
    assertAvgIs(Values.asSequence(DecimalType.get(), 0.5, 2.5, 3.8, 5.2), 3.0);
  }

  @Test
  public void test_avg_sequenceContainsNullReturnsNullValue() {
    assertAvgIs(Values.asSequence(DecimalType.get(), 1, null, 3, 4), null);
  }

  @Test
  public void test_avg_nullSequenceReturnsNullValue() {
    assertAvgIs(DecimalType.get().nullSequence(), null);
  }

  @Test
  public void test_avg_onNonSequenceReturnsValue() {
    assertAvgIs(IntegerType.get().valueOf(4), 4);
    assertAvgIs(DecimalType.get().valueOf(4), 4.0);
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void test_avg_onNonNumericTypeThrowsAnException() {
    assertAvgIs(TextType.get().valueOf("This is not a number, so you can't call sum()"), null);
  }

  @Test
  public void test_avg_emptySequence() {
    assertAvgIs(DecimalType.get().sequenceOf(Collections.<Value> emptyList()), null);
  }

  private void assertAvgIs(Value valueToSum, Number expectedSum) {
    if(expectedSum instanceof Integer) {
      expectedSum = new Long(expectedSum.longValue());
    }
    ScriptableValue result = super.evaluate("avg()", valueToSum);
    Assert.assertNotNull(result);
    Assert.assertEquals(expectedSum, result.getValue().getValue());
  }

  // sum

  @Test
  public void test_sum_integerType() {
    assertSumIs(Values.asSequence(IntegerType.get(), 1, 2, 3, 4), 10);
  }

  @Test
  public void test_sum_decimalType() {
    assertSumIs(Values.asSequence(DecimalType.get(), 1, 2, 3, 4), 10.0);
    assertSumIs(Values.asSequence(DecimalType.get(), 0.5, 2.5, 3.8, 4.2), 11.0);
  }

  @Test
  public void test_sum_sequenceContainsNullReturnsNullValue() {
    assertSumIs(Values.asSequence(DecimalType.get(), 1, null, 3, 4), null);
  }

  @Test
  public void test_sum_nullSequenceReturnsNullValue() {
    assertSumIs(DecimalType.get().nullSequence(), null);
  }

  @Test
  public void test_sum_onNonSequenceReturnsValue() {
    assertSumIs(IntegerType.get().valueOf(4), 4);
    assertSumIs(DecimalType.get().valueOf(4), 4.0);
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void test_sum_onNonNumericTypeThrowsAnException() {
    assertSumIs(TextType.get().valueOf("This is not a number, so you can't call sum()"), null);
  }

  @Test
  public void test_sum_emptySequence() {
    assertSumIs(DecimalType.get().sequenceOf(Collections.<Value> emptyList()), 0.0);
  }

  private void assertSumIs(Value valueToSum, Number expectedSum) {
    if(expectedSum instanceof Integer) {
      expectedSum = new Long(expectedSum.longValue());
    }
    ScriptableValue result = super.evaluate("sum()", valueToSum);
    Assert.assertNotNull(result);
    Assert.assertEquals(expectedSum, result.getValue().getValue());
  }

  // push

  @Test
  public void test_push() {
    assertPushIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "4", Values.asSequence(IntegerType.get(), 1, 2, 3, 4));
  }

  @Test
  public void test_push_multipleValues() {
    assertPushIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "4,5", Values.asSequence(IntegerType.get(), 1, 2, 3, 4, 5));
  }

  @Test
  public void test_push_convertsTypes() {
    assertPushIs(Values.asSequence(TextType.get(), 1, 2, 3), "4,5", Values.asSequence(TextType.get(), 1, 2, 3, 4, 5));
  }

  @Test
  public void test_push_nullSequenceProducesNull() {
    assertPushIs(TextType.get().nullSequence(), "'this will not be pushed'", TextType.get().nullSequence());
  }

  @Test
  public void test_push_acceptsNullArgument() {
    assertPushIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "null", Values.asSequence(IntegerType.get(), 1, 2, 3, null));
  }

  @Test
  public void test_push_acceptsNonSequenceOperand() {
    assertPushIs(TextType.get().valueOf("this is not a sequence"), "'neither is this'", Values.asSequence(TextType.get(), "this is not a sequence", "neither is this"));
  }

  private void assertPushIs(Value valueToPushInto, String push, Value expected) {
    ScriptableValue result = super.evaluate("push(" + push + ")", valueToPushInto);
    Assert.assertNotNull(result);
    if(expected.isNull()) {
      Assert.assertTrue(result.getValue().isNull());
    } else {
      Assert.assertTrue(Iterables.elementsEqual(result.getValue().asSequence().getValue(), expected.asSequence().getValue()));
    }
  }

  @SuppressWarnings("serial")
  private class MyScriptableValueCustomSortAsc extends ScriptableValue {

    public MyScriptableValueCustomSortAsc(Scriptable scope, Value value) {
      super(scope, value);
    }

    @SuppressWarnings("unused")
    public int sort(ScriptableValue value1, ScriptableValue value2) {
      return (int) ((Long) value1.getValue().getValue() - (Long) value2.getValue().getValue());
    }
  }

  @SuppressWarnings("serial")
  private class MyScriptableValueCustomSortDesc extends ScriptableValue {

    public MyScriptableValueCustomSortDesc(Scriptable scope, Value value) {
      super(scope, value);
    }

    @SuppressWarnings("unused")
    public int sort(ScriptableValue value1, ScriptableValue value2) {
      return (int) ((Long) value2.getValue().getValue() - (Long) value1.getValue().getValue());
    }
  }

  public MyScriptableValueCustomSortAsc newValueAsc(Value value) {
    return new MyScriptableValueCustomSortAsc(getSharedScope(), value);
  }

  public MyScriptableValueCustomSortDesc newValueDesc(Value value) {
    return new MyScriptableValueCustomSortDesc(getSharedScope(), value);
  }
}

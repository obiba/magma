package org.obiba.magma.js.methods;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

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
    ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(valueSequence.getValues().get(0), is(TextType.get().valueOf("A")));
    assertThat(valueSequence.getValues().get(1), is(TextType.get().valueOf("B")));
    assertThat(valueSequence.getValues().get(2), is(TextType.get().valueOf("C")));
    assertThat(valueSequence.getValues().get(3), is(TextType.get().valueOf("D")));
  }

  @Test
  public void testSortIntegerNaturalOrder() throws Exception {
    ValueSequence valueSequence = IntegerType.get().sequenceOf("4,3,1,2");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, null, null);
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
    ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, new Object[] { funObj }, null);
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
    ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, new Object[] { funObj }, null);
    assertThat(valueSequence.getValues().get(0), is(IntegerType.get().valueOf(1)));
    assertThat(valueSequence.getValues().get(1), is(IntegerType.get().valueOf(2)));
    assertThat(valueSequence.getValues().get(2), is(IntegerType.get().valueOf(3)));
    assertThat(valueSequence.getValues().get(3), is(IntegerType.get().valueOf(4)));
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

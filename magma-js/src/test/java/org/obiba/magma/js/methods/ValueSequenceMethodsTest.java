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
import java.util.Collections;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
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

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters", "ReuseOfLocalVariable" })
public class ValueSequenceMethodsTest extends AbstractJsTest {

  @Test
  public void testIsSequence() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("A");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.isSequence(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue().getValue()).isEqualTo(Boolean.TRUE);
  }

  @Test
  public void testNullSequenceIsSequence() throws Exception {
    ValueSequence valueSequence = TextType.get().nullSequence();
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.isSequence(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue().getValue()).isEqualTo(Boolean.TRUE);
  }

  @Test
  public void testIsNotSequence() throws Exception {
    Value value = TextType.get().valueOf("A");
    ScriptableValue scriptableValue = newValue(value);
    ScriptableValue result = ValueSequenceMethods.isSequence(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue().getValue()).isEqualTo(Boolean.FALSE);
  }

  @Test
  public void testNullValueIsNotSequence() throws Exception {
    Value value = TextType.get().nullValue();
    ScriptableValue scriptableValue = newValue(value);
    ScriptableValue result = ValueSequenceMethods.isSequence(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue().getValue()).isEqualTo(Boolean.FALSE);
  }

  @Test
  public void testFirstOfOne() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("A");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(valueSequence.get(0));
  }

  @Test
  public void testFirstOfTwo() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(valueSequence.get(0));
  }

  @Test
  public void testFirstOnNonSequence() throws Exception {
    Value value = TextType.get().valueOf("A-Value");
    ScriptableValue scriptableValue = newValue(value);
    ScriptableValue result = ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(value);
  }

  @Test
  public void testFirstOnNullOperand() throws Exception {
    ScriptableValue scriptableValue = newValue(TextType.get().nullValue());
    ScriptableValue result = ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(scriptableValue.getValue());
  }

  @Test
  public void testFirstOfNone() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf(new ArrayList<Value>());
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.first(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().nullValue());
  }

  // last()

  @Test
  public void testLastOfOne() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("A");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(valueSequence.get(0));
  }

  @Test
  public void testLastOfTwo() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(valueSequence.get(1));
  }

  @Test
  public void testLastOnNonSequence() throws Exception {
    Value value = TextType.get().valueOf("A-Value");
    ScriptableValue scriptableValue = newValue(value);
    ScriptableValue result = ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(value);
  }

  @Test
  public void testLastOnNullOperand() throws Exception {
    ScriptableValue scriptableValue = newValue(TextType.get().nullValue());
    ScriptableValue result = ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(scriptableValue.getValue());
  }

  @Test
  public void testLastOfNone() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf(new ArrayList<Value>());
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.last(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().nullValue());
  }

  // size()

  @Test
  public void testSizeOfOne() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("A");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(valueSequence.getSize()));
  }

  @Test
  public void testSizeOfTwo() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(valueSequence.getSize()));
  }

  @Test
  public void testSizeOnNonSequence() throws Exception {
    Value value = TextType.get().valueOf("A-Value");
    ScriptableValue scriptableValue = newValue(value);
    ScriptableValue result = ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));
  }

  @Test
  public void testSizeOnNullOperand() throws Exception {
    ScriptableValue scriptableValue = newValue(TextType.get().nullValue());
    ScriptableValue result = ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().nullValue());
  }

  @Test
  public void testSizeOfNone() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf(new ArrayList<Value>());
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods.size(Context.getCurrentContext(), scriptableValue, null, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(valueSequence.getSize()));
  }

  // value(int index)

  @Test
  public void testValueFirstItemExists() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { 0 }, null);
    assertThat(result.getValue()).isEqualTo(valueSequence.get(0));
  }

  @Test
  public void testValueSecondItemExists() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { 1 }, null);
    assertThat(result.getValue()).isEqualTo(valueSequence.get(1));
  }

  @Test
  public void testValueIndexOutOfMaxBound() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { 2 }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().nullValue());
  }

  @Test
  public void testValueIndexOutOfMinBound() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { -1 }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().nullValue());
  }

  @Test
  public void testValueIndexNonIntegerType() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { "One" }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().nullValue());
  }

  @Test
  public void testNullSequenceValueAt() throws Exception {
    ValueSequence valueSequence = TextType.get().nullSequence();
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { 2 }, null);
    assertThat(result.getValue()).isEqualTo(TextType.get().nullValue());
  }

  @Test
  public void testValueAtValue() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { IntegerType.get().valueOf(0) }, null);
    assertThat(result.getValue()).isEqualTo(valueSequence.get(0));
  }

  @Test
  public void testValueAtTextValue() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { TextType.get().valueOf("0") }, null);
    assertThat(result.getValue()).isEqualTo(valueSequence.get(0));
  }

  @Test
  public void testValueAtNullValue() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
      .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { TextType.get().nullValue() }, null);
    assertThat(result.getValue().isNull()).isTrue();
    result = ValueSequenceMethods
      .valueAt(Context.getCurrentContext(), scriptableValue, new Object[] { null }, null);
    assertThat(result.getValue().isNull()).isTrue();
  }

  @Test
  public void testValueIndexOf() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\", \"A\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .indexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "A" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(0));
    result = ValueSequenceMethods
        .indexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "B" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));
    result = ValueSequenceMethods
        .indexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "C" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(-1));
    result = ValueSequenceMethods
        .indexOf(Context.getCurrentContext(), scriptableValue, new Object[] { }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(-1));
  }

  @Test
  public void testNullValueIndexOf() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\",, \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .indexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "A" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(0));
    result = ValueSequenceMethods
        .indexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "B" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(2));
    result = ValueSequenceMethods
        .indexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "C" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(-1));
    result = ValueSequenceMethods
        .indexOf(Context.getCurrentContext(), scriptableValue, new Object[] { TextType.get().nullValue() }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));
  }

  @Test
  public void testValueLastIndexOf() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\", \"B\", \"A\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .lastIndexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "A" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(2));
    result = ValueSequenceMethods
        .lastIndexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "B" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));
    result = ValueSequenceMethods
        .lastIndexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "C" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(-1));
    result = ValueSequenceMethods
        .lastIndexOf(Context.getCurrentContext(), scriptableValue, new Object[] { }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(-1));
  }

  @Test
  public void testNullValueLastIndexOf() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"A\",, \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    ScriptableValue result = ValueSequenceMethods
        .lastIndexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "A" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(0));
    result = ValueSequenceMethods
        .lastIndexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "B" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(2));
    result = ValueSequenceMethods
        .lastIndexOf(Context.getCurrentContext(), scriptableValue, new Object[] { "C" }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(-1));
    result = ValueSequenceMethods
        .lastIndexOf(Context.getCurrentContext(), scriptableValue, new Object[] { TextType.get().nullValue() }, null);
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));
  }

  // sort()

  @Test
  public void testSortTextNaturalOrder() throws Exception {
    ValueSequence valueSequence = TextType.get().sequenceOf("\"D\", \"C\", \"A\", \"B\"");
    ScriptableValue scriptableValue = newValue(valueSequence);
    valueSequence = ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, null, null).getValue()
        .asSequence();
    assertThat(valueSequence.getValues().get(0)).isEqualTo(TextType.get().valueOf("A"));
    assertThat(valueSequence.getValues().get(1)).isEqualTo(TextType.get().valueOf("B"));
    assertThat(valueSequence.getValues().get(2)).isEqualTo(TextType.get().valueOf("C"));
    assertThat(valueSequence.getValues().get(3)).isEqualTo(TextType.get().valueOf("D"));
  }

  @Test
  public void testSortIntegerNaturalOrder() throws Exception {
    ValueSequence valueSequence = IntegerType.get().sequenceOf("4,3,1,2");
    ScriptableValue scriptableValue = newValue(valueSequence);
    valueSequence = ValueSequenceMethods.sort(Context.getCurrentContext(), scriptableValue, null, null).getValue()
        .asSequence();
    assertThat(valueSequence.getValues().get(0)).isEqualTo(IntegerType.get().valueOf(1));
    assertThat(valueSequence.getValues().get(1)).isEqualTo(IntegerType.get().valueOf(2));
    assertThat(valueSequence.getValues().get(2)).isEqualTo(IntegerType.get().valueOf(3));
    assertThat(valueSequence.getValues().get(3)).isEqualTo(IntegerType.get().valueOf(4));
  }

  @Test
  public void testSortIntegerDescendingUsingSortFunction() throws Exception {
    ValueSequence valueSequence = IntegerType.get().sequenceOf("4,3,1,2");

    MyScriptableValueCustomSortDesc scriptableValue = newValueDesc(valueSequence);
    FunctionObject funObj = new FunctionObject("sort",
        scriptableValue.getClass().getMethod("sort", new Class[] { ScriptableValue.class, ScriptableValue.class }),
        scriptableValue);
    valueSequence = ValueSequenceMethods
        .sort(Context.getCurrentContext(), scriptableValue, new Object[] { funObj }, null).getValue().asSequence();
    assertThat(valueSequence.getValues().get(0)).isEqualTo(IntegerType.get().valueOf(4));
    assertThat(valueSequence.getValues().get(1)).isEqualTo(IntegerType.get().valueOf(3));
    assertThat(valueSequence.getValues().get(2)).isEqualTo(IntegerType.get().valueOf(2));
    assertThat(valueSequence.getValues().get(3)).isEqualTo(IntegerType.get().valueOf(1));
  }

  @Test
  public void testSortIntegerAscendingUsingSortFunction() throws Exception {
    ValueSequence valueSequence = IntegerType.get().sequenceOf("4,3,1,2");

    MyScriptableValueCustomSortAsc scriptableValue = newValueAsc(valueSequence);
    FunctionObject funObj = new FunctionObject("sort",
        scriptableValue.getClass().getMethod("sort", new Class[] { ScriptableValue.class, ScriptableValue.class }),
        scriptableValue);
    valueSequence = ValueSequenceMethods
        .sort(Context.getCurrentContext(), scriptableValue, new Object[] { funObj }, null).getValue().asSequence();
    assertThat(valueSequence.getValues().get(0)).isEqualTo(IntegerType.get().valueOf(1));
    assertThat(valueSequence.getValues().get(1)).isEqualTo(IntegerType.get().valueOf(2));
    assertThat(valueSequence.getValues().get(2)).isEqualTo(IntegerType.get().valueOf(3));
    assertThat(valueSequence.getValues().get(3)).isEqualTo(IntegerType.get().valueOf(4));
  }

  @Test
  public void test_sort_nullSequenceReturnsNullSequence() {
    assertMethod("sort()", IntegerType.get().nullSequence(), IntegerType.get().nullSequence());
  }

  @Test
  public void test_sort_nonSequenceReturnsSingleValue() {
    assertMethod("sort()", IntegerType.get().valueOf(4), IntegerType.get().valueOf(4));
  }

  // filter

  @Test
  public void test_filter_integerValues() {
    assertFilterIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "function(v) { return v.ge(2) }",
        Values.asSequence(IntegerType.get(), 2, 3));
    assertFilterIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "function(v) { return v.ge(4) }",
        IntegerType.get().nullSequence());
  }

  @Test
  public void test_filter_integerPositions() {
    assertFilterIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "function(v,i) { return i>0 }",
        Values.asSequence(IntegerType.get(), 2, 3));
    assertFilterIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "function(v,i) { return i>4 }",
        IntegerType.get().nullSequence());
  }

  @Test
  public void test_filter_integerValuesValue() {
    assertFilterIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "function(v) { return v.ge(2).value() }",
        Values.asSequence(IntegerType.get(), 2, 3));
  }

  @Test
  public void test_filter_emptyIntegerValues() {
    assertFilterIs(Values.asSequence(IntegerType.get()), "function(v) { return v.ge(2) }",
        IntegerType.get().nullSequence());
  }

  @Test
  public void test_filter_nullValueSequence() {
    assertFilterIs(IntegerType.get().nullSequence(), "function(v) { return v.ge(2) }",
        IntegerType.get().nullSequence());
  }

  @Test
  public void test_filter_nullValue() {
    assertFilterIs(IntegerType.get().nullValue(), "function(v) { return v.ge(2) }",
        IntegerType.get().nullSequence());
  }

  @Test
  public void test_filter_nullValues() {
    assertFilterIs(Values.asSequence(IntegerType.get(), 1, null, 3), "function(v) { return v.isNull().not() }",
        Values.asSequence(IntegerType.get(), 1, 3));
  }

  @Test
  public void test_filter_singleValue() {
    String function = "function(v) { return v.ge(2) }";
    assertFilterIs(IntegerType.get().valueOf(2), function, Values.asSequence(IntegerType.get(), 2));
    assertFilterIs(IntegerType.get().valueOf(1), function, IntegerType.get().nullSequence());
  }

  @Test
  public void test_filter_trim() {
    assertFilterIs(Values.asSequence(IntegerType.get(),  null, 1, 2, 3, null, null), "function(v) { return v.isNull().not() }",
        Values.asSequence(IntegerType.get(), 1, 2, 3));
    assertTrimmerIs(Values.asSequence(IntegerType.get(),  null, 1, 2, 3, null, null), Values.asSequence(IntegerType.get(), 1, 2, 3));
  }

  @Test
  public void test_filter_subsetFrom() {
    assertFilterIs(Values.asSequence(IntegerType.get(),  1, 2, 3, 4), "function(v,i) { return i>=1 }",
        Values.asSequence(IntegerType.get(), 2, 3, 4));
    assertSubsetIs(Values.asSequence(IntegerType.get(),  1, 2, 3, 4), "1",
        Values.asSequence(IntegerType.get(), 2, 3, 4));
    assertSubsetIs(Values.asSequence(IntegerType.get(),  1, 2, 3, 4), "5",
        IntegerType.get().nullSequence());
  }

  @Test
  public void test_filter_subsetFromTo() {
    assertFilterIs(Values.asSequence(IntegerType.get(),  1, 2, 3, 4), "function(v,i) { return i>=1 && i<3 }",
        Values.asSequence(IntegerType.get(), 2, 3));
    assertSubsetIs(Values.asSequence(IntegerType.get(),  1, 2, 3, 4), "1,3",
        Values.asSequence(IntegerType.get(), 2, 3));
  }

  // reduce

  @Test
  public void test_filter_reduce_to_sum() {
    assertReduceIs(Values.asSequence(IntegerType.get(), 1, 2, 3, 4), "function(a,v,i) { return a.plus(v) }",
        IntegerType.get().valueOf(10));
  }

  @Test
  public void test_filter_reduce_null_value_to_sum() {
    assertReduceIs(IntegerType.get().nullSequence(), "function(a,v,i) { return a.plus(v) }",
        IntegerType.get().nullValue());
  }

  @Test
  public void test_filter_reduce_single_value_to_sum() {
    assertReduceIs(IntegerType.get().valueOf(1), "function(a,v,i) { return a.plus(v) }",
        IntegerType.get().valueOf(1));
  }

  @Test
  public void test_filter_reduce_sequence_with_null_to_sum() {
    assertReduceIs(Values.asSequence(IntegerType.get(),  1, 2, null, 3, 4), "function(a,v,i) { return a.plus(v) }",
        IntegerType.get().valueOf(10));
    assertReduceIs(Values.asSequence(IntegerType.get(),  null, 1, 2, 3, 4), "function(a,v,i) { return a.plus(v) }",
        IntegerType.get().valueOf(10));
  }

  @Test
  public void test_filter_reduce_sequence_with_nulls_to_sum() {
    assertReduceIs(Values.asSequence(IntegerType.get(),  null, null, null), "function(a,v,i) { return a.plus(v) }",
        IntegerType.get().nullValue());
  }

  @Test
  public void test_filter_reduce_sequence_with_nulls_to_not_null_sum() {
    assertReduceIs(Values.asSequence(IntegerType.get(),  null, null, null), "function(a,v,i) { return a.plus(v) }, 0",
        IntegerType.get().valueOf(0));
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
  public void test_avg_sequenceContainsNullReturnsNotNullValue() {
    assertAvgIs(Values.asSequence(DecimalType.get(), 1, null, 3, 4), 2.0);
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
    assertAvgIs(TextType.get().valueOf("This is not a number, so you can't call avg()"), null);
  }

  @Test
  public void test_avg_emptySequence() {
    assertAvgIs(DecimalType.get().sequenceOf(Collections.<Value>emptyList()), null);
  }

  @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
  private void assertAvgIs(Value valueToSum, @Nullable Number expectedSum) {
    if(expectedSum instanceof Integer) {
      expectedSum = expectedSum.longValue();
    }
    ScriptableValue result = evaluate("avg()", valueToSum);
    assertThat(result).isNotNull();
    if(expectedSum == null) {
      assertThat(result.getValue().isNull()).isTrue();
    } else {
      assertThat(result.getValue().getValue()).isEqualTo(expectedSum);
    }
  }

  // stddev

  @Test
  public void test_stddev_integerType() {
    assertStdDevIs(Values.asSequence(IntegerType.get(), 2, 4, 4, 4, 5, 5, 7, 9), 2.0);
  }

  @Test
  public void test_stddev_decimalType() {
    assertStdDevIs(Values.asSequence(DecimalType.get(), 2, 4, 4, 4, 5, 5, 7, 9), 2.0);
  }

  @Test
  public void test_stddev_sequenceContainsNullReturnsNotNullValue() {
    assertStdDevIs(Values.asSequence(DecimalType.get(), 1, null, 3, 4), 1.224744871391589);
  }

  @Test
  public void test_stddev_nullSequenceReturnsNullValue() {
    assertStdDevIs(DecimalType.get().nullSequence(), null);
  }

  @Test
  public void test_stddev_onNonSequenceReturnsValue() {
    assertStdDevIs(IntegerType.get().valueOf(4), 0.0);
    assertStdDevIs(DecimalType.get().valueOf(4), 0.0);
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void test_stddev_onNonNumericTypeThrowsAnException() {
    assertStdDevIs(TextType.get().valueOf("This is not a number, so you can't call stddev()"), null);
  }

  @Test
  public void test_stddev_emptySequence() {
    assertStdDevIs(DecimalType.get().sequenceOf(Collections.<Value>emptyList()), null);
  }

  @SuppressWarnings({ "AssignmentToMethodParameter", "PMD.AvoidReassigningParameters" })
  private void assertStdDevIs(Value testValue, @Nullable Number expected) {
    if(expected instanceof Integer) {
      expected = expected.longValue();
    }
    ScriptableValue result = evaluate("stddev()", testValue);
    assertThat(result).isNotNull();
    if(expected == null) {
      assertThat(result.getValue().isNull()).isTrue();
    } else {
      assertThat(result.getValue().getValue()).isEqualTo(expected);
    }
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
  public void test_sum_sequenceContainsNullReturnsNotNullValue() {
    assertSumIs(Values.asSequence(DecimalType.get(), 1, null, 3, 4), 8.0);
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
    assertSumIs(DecimalType.get().sequenceOf(Collections.<Value>emptyList()), 0.0);
  }

  private void assertSumIs(Value valueToSum, @Nullable Number expectedSum) {
    if(expectedSum instanceof Integer) {
      expectedSum = expectedSum.longValue();
    }
    ScriptableValue result = evaluate("sum()", valueToSum);
    assertThat(result).isNotNull();
    if(expectedSum == null) {
      assertThat(result.getValue().isNull()).isTrue();
    } else {
      assertThat(result.getValue().getValue()).isEqualTo(expectedSum);
    }
  }

  // push

  @Test
  public void test_push() {
    assertPushIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "4", Values.asSequence(IntegerType.get(), 1, 2, 3, 4));
  }

  @Test
  public void test_append() {
    assertAppendIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "4", Values.asSequence(IntegerType.get(), 1, 2, 3, 4));
  }

  @Test
  public void test_prepend() {
    assertPrependIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "4", Values.asSequence(IntegerType.get(), 4, 1, 2, 3));
  }

  @Test
  public void test_insertAt() {
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 0, "4", Values.asSequence(IntegerType.get(), 4, 1, 2, 3));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 1, "4", Values.asSequence(IntegerType.get(), 1, 4, 2, 3));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 2, "4", Values.asSequence(IntegerType.get(), 1, 2, 4, 3));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 3, "4", Values.asSequence(IntegerType.get(), 1, 2, 3, 4));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 4, "4", Values.asSequence(IntegerType.get(), 1, 2, 3, null, 4));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 5, "4", Values.asSequence(IntegerType.get(), 1, 2, 3, null, null, 4));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 0, new String[] {"4","5","6"}, Values.asSequence(IntegerType.get(), 4, 5, 6, 1, 2, 3));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 1, new String[] {"4","5","6"}, Values.asSequence(IntegerType.get(), 1, 4, 5, 6, 2, 3));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 4, new String[] {"4","5","6"}, Values.asSequence(IntegerType.get(), 1, 2, 3, null, 4, 5, 6));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 0, new String[] {"4","null","6"}, Values.asSequence(IntegerType.get(), 4, null, 6, 1, 2, 3));
    assertInsertAtIs(Values.asSequence(IntegerType.get(), 1, 2, 3), 0, "4", Values.asSequence(IntegerType.get(), 4, 1, 2, 3));
    assertInsertAtIs(IntegerType.get().valueOf(1), 0, "4", Values.asSequence(IntegerType.get(), 4, 1));
    assertInsertAtIs(IntegerType.get().valueOf(1), 1, "4", Values.asSequence(IntegerType.get(),  1, 4));
  }

  @Test
  public void test_insertAt_null() {
    assertInsertAtIs(IntegerType.get().nullValue(), 0, "4", Values.asSequence(IntegerType.get(),  4));
    assertInsertAtIs(IntegerType.get().nullSequence(), 0, "4", Values.asSequence(IntegerType.get(),  4));
  }

  @Test
  public void test_push_nothing() {
    assertPushIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "", Values.asSequence(IntegerType.get(), 1, 2, 3));
  }

  @Test
  public void test_append_nothing() {
    assertAppendIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "", Values.asSequence(IntegerType.get(), 1, 2, 3));
  }

  @Test
  public void test_prepend_nothing() {
    assertPrependIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "", Values.asSequence(IntegerType.get(), 1, 2, 3));
  }

  @Test
  public void test_push_multipleValues() {
    assertPushIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "4,5", Values.asSequence(IntegerType.get(), 1, 2, 3, 4, 5));
  }

  @Test
  public void test_append_multipleValues() {
    assertAppendIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "4,5", Values.asSequence(IntegerType.get(), 1, 2, 3, 4, 5));
  }

  @Test
  public void test_prepend_multipleValues() {
    assertPrependIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "4,5", Values.asSequence(IntegerType.get(), 4, 5, 1, 2, 3));
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
  public void test_append_nullSequence() {
    assertAppendIs(TextType.get().nullSequence(), "'foo'", Values.asSequence(TextType.get(), "foo"));
  }

  @Test
  public void test_prepend_nullSequence() {
    assertPrependIs(TextType.get().nullSequence(), "'foo'", Values.asSequence(TextType.get(), "foo"));
  }

  @Test
  public void test_push_acceptsNullArgument() {
    assertPushIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "null", Values.asSequence(IntegerType.get(), 1, 2, 3, null));
  }

  @Test
  public void test_append_acceptsNullArgument() {
    assertAppendIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "null", Values.asSequence(IntegerType.get(), 1, 2, 3, null));
  }

  @Test
  public void test_prepend_acceptsNullArgument() {
    assertPrependIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "null", Values.asSequence(IntegerType.get(), null, 1, 2, 3));
  }

  @Test
  public void test_push_acceptsNonSequenceOperand() {
    assertPushIs(TextType.get().valueOf("this is not a sequence"), "'neither is this'",
        Values.asSequence(TextType.get(), "this is not a sequence", "neither is this"));
  }

  @Test
  public void test_append_acceptsNonSequenceOperand() {
    assertAppendIs(TextType.get().valueOf("this is not a sequence"), "'neither is this'",
        Values.asSequence(TextType.get(), "this is not a sequence", "neither is this"));
  }

  @Test
  public void test_prepend_acceptsNonSequenceOperand() {
    assertPrependIs(TextType.get().valueOf("this is not a sequence"), "'neither is this'",
        Values.asSequence(TextType.get(), "neither is this", "this is not a sequence"));
  }

  private void assertPushIs(Value valueToPushInto, String push, Value expected) {
    assertPendIs("push", valueToPushInto, push, expected);
  }

  private void assertAppendIs(Value valueToPushInto, String push, Value expected) {
    assertPendIs("append", valueToPushInto, push, expected);
  }

  private void assertPrependIs(Value valueToPushInto, String push, Value expected) {
    assertPendIs("prepend", valueToPushInto, push, expected);
  }

  private void assertPendIs(String pendMethod, Value valueToPushInto, String push, Value expected) {
    ScriptableValue result = evaluate(pendMethod + "(" + push + ")", valueToPushInto);
    assertThat(result).isNotNull();

    if(expected.isNull()) {
      assertThat(result.getValue().isNull()).isTrue();
    } else {
      assertThat(result.getValue().asSequence().getValue()).isEqualTo(expected.asSequence().getValue());
    }
  }

  private void assertInsertAtIs(Value valueToPushInto, int position, String push, Value expected) {
    assertInsertAtIs(valueToPushInto, position, new String[] { push }, expected);
  }

  private void assertInsertAtIs(Value valueToPushInto, int position, String[] push, Value expected) {
    String pushes = Joiner.on(",").join(push);
    ScriptableValue result = evaluate("insertAt(" + position + "," + pushes + ")", valueToPushInto);
    assertThat(result).isNotNull();

    if(expected.isNull()) {
      assertThat(result.getValue().isNull()).isTrue();
    } else {
      assertThat(result.getValue().asSequence().getValue()).isEqualTo(expected.asSequence().getValue());
    }
  }

  // join
  @Test
  public void test_join() {
    assertJoinIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "", TextType.get().valueOf("123"));
  }

  @Test
  public void test_join_not_a_sequence() {
    assertJoinIs(IntegerType.get().valueOf(1), "', ','[', ']'", TextType.get().valueOf("[1]"));
  }

  @Test
  public void test_join_null() {
    assertJoinIs(IntegerType.get().nullSequence(), "", TextType.get().nullValue());
  }

  @Test
  public void test_join_delimiter() {
    assertJoinIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "', '", TextType.get().valueOf("1, 2, 3"));
  }

  @Test
  public void test_join_delimiter_prefix() {
    assertJoinIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "', ','=> '", TextType.get().valueOf("=> 1, 2, 3"));
  }

  @Test
  public void test_join_delimiter_prefix_suffix() {
    assertJoinIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "', ','[', ']'", TextType.get().valueOf("[1, 2, 3]"));
  }

  @Test
  public void test_join_delimiter_prefix_suffix_empty() {
    assertJoinIs(Values.asSequence(IntegerType.get()), "', ','[', ']'", TextType.get().valueOf(""));
  }

  private void assertJoinIs(Value valueToJoin, String args, Value expected) {
    ScriptableValue result = evaluate("join(" + args + ")", valueToJoin);
    assertThat(result).isNotNull();

    if(expected.isNull()) {
      assertThat(result.getValue().isNull()).isTrue();
    } else {
      assertThat(expected.getValue()).isEqualTo(result.getValue().getValue());
    }
  }

  // zip
  @Test
  public void test_zip_concat() {
    assertZipIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "'foo', function(o1,o2) { return o1.concat(o2); }",
        Values.asSequence(TextType.get(), "1foo", "2foo", "3foo"));
  }

  @Test
  public void test_zip_concat_value() {
    assertZipIs(Values.asSequence(IntegerType.get(), 1, 2, 3),
        "newValue('foo'), function(o1,o2) { return o1.concat(o2); }",
        Values.asSequence(TextType.get(), "1foo", "2foo", "3foo"));
  }

  @Test
  public void test_zip_concat_value_sequence() {
    assertZipIs(Values.asSequence(IntegerType.get(), 1, 2, 3),
        "newValue('foo').push('bar'), function(o1,o2) { return o1.concat(o2); }",
        Values.asSequence(TextType.get(), "1foo", "2bar", "3null"));
  }

  @Test
  public void test_zip_concat_value_sequence_longer() {
    assertZipIs(Values.asSequence(IntegerType.get(), 1, 2, 3),
        "newValue('foo').push('bar').push('patate').push('pwel'), function(o1,o2) { return o1.concat(o2); }",
        Values.asSequence(TextType.get(), "1foo", "2bar", "3patate", "nullpwel"));
  }

  @Test
  public void test_zip_concat_empty() {
    assertZipIs(Values.asSequence(IntegerType.get()), "'foo', function(o1,o2) { return o1.concat(o2); }",
        Values.asSequence(TextType.get(), "nullfoo"));
  }

  @Test
  public void test_zip_concat_null() {
    assertZipIs(IntegerType.get().nullSequence(), "'foo', function(o1,o2) { return o1.concat(o2); }",
        Values.asSequence(TextType.get(), "nullfoo"));
  }

  @Test
  public void test_zip_plus() {
    assertZipIs(Values.asSequence(IntegerType.get(), 1, 2, 3), "1, function(o1,o2) { return o1.plus(o2); }",
        Values.asSequence(IntegerType.get(), 2, 3, 4));
  }

  private void assertZipIs(Value valueToZip, String args, Value expected) {
    ScriptableValue result = evaluate("zip(" + args + ")", valueToZip);
    assertResultIs(result, expected);
  }

  private void assertFilterIs(Value valueToFilter, String args, Value expected) {
    ScriptableValue result = evaluate("filter(" + args + ")", valueToFilter);
    assertResultIs(result, expected);
  }

  private void assertTrimmerIs(Value valueToFilter, Value expected) {
    ScriptableValue result = evaluate("trimmer()", valueToFilter);
    assertResultIs(result, expected);
  }

  private void assertSubsetIs(Value valueToFilter, String args, Value expected) {
    ScriptableValue result = evaluate("subset(" + args + ")", valueToFilter);
    assertResultIs(result, expected);
  }

  private void assertReduceIs(Value valueToReduce, String args, Value expected) {
    ScriptableValue result = evaluate("reduce(" + args + ")", valueToReduce);
    assertResultIs(result, expected);
  }

  private void assertResultIs(ScriptableValue result, Value expected) {
    assertThat(result).isNotNull();

    if(expected.isNull()) {
      assertThat(result.getValue().isNull()).isTrue();
    } else if (expected.isSequence()) {
      assertThat(result.getValue().isSequence()).isTrue();
      assertThat(result.getValue().asSequence().getValue()).isEqualTo(expected.asSequence().getValue());
    } else {
      assertThat(result.getValue().isSequence()).isFalse();
      assertThat(result.getValue().getValue()).isEqualTo(expected.getValue());
    }
  }

  @SuppressWarnings("serial")
  private static class MyScriptableValueCustomSortAsc extends ScriptableValue {

    private MyScriptableValueCustomSortAsc(Scriptable scope, Value value) {
      super(scope, value);
    }

    public int sort(ScriptableValue value1, ScriptableValue value2) {
      return (int) ((Long) value1.getValue().getValue() - (Long) value2.getValue().getValue());
    }
  }

  @SuppressWarnings("serial")
  private static class MyScriptableValueCustomSortDesc extends ScriptableValue {

    private MyScriptableValueCustomSortDesc(Scriptable scope, Value value) {
      super(scope, value);
    }

    public int sort(ScriptableValue value1, ScriptableValue value2) {
      return (int) ((Long) value2.getValue().getValue() - (Long) value1.getValue().getValue());
    }
  }

  private MyScriptableValueCustomSortAsc newValueAsc(Value value) {
    return new MyScriptableValueCustomSortAsc(getSharedScope(), value);
  }

  private MyScriptableValueCustomSortDesc newValueDesc(Value value) {
    return new MyScriptableValueCustomSortDesc(getSharedScope(), value);
  }
}

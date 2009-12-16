package org.obiba.magma.js.methods;

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
  public void testAllCalledOnNullValueReturnsNull() {
    ScriptableValue nullValue = newValue(TextType.get().nullValue());
    ScriptableValue result = BooleanMethods.all(Context.getCurrentContext(), nullValue, new String[] { "CAT1", "CAT3", "CAT5" }, null);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.getValue().isNull());
  }

  @Test
  public void testAnyWithNullValue() {
    ScriptableValue value = newValue(TextType.get().valueOf(null));
    ScriptableValue result = BooleanMethods.any(Context.getCurrentContext(), value, new String[] { "CAT1", "CAT2" }, null);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.getValue().isNull());
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
  public void testTrueAndFalseReturnsTrue() {
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
}

package org.obiba.magma.js.methods;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.js.AbstractScriptableValueTest;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

public class CompareMethodsTest extends AbstractScriptableValueTest {

  @Test
  public void testIntegerCompareInteger() throws Exception {
    ScriptableValue integerSeven = newValue(IntegerType.get().valueOf(7));
    ScriptableValue integerZero = newValue(IntegerType.get().valueOf(0));
    ScriptableValue result = CompareMethods.compare(Context.getCurrentContext(), integerSeven, new Object[] { integerZero }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));
  }

  @Test
  public void testIntegerCompareDecimal() throws Exception {
    ScriptableValue integerSeven = newValue(IntegerType.get().valueOf(7));
    ScriptableValue decimalFourteen = newValue(DecimalType.get().valueOf(14.0));
    ScriptableValue result = CompareMethods.compare(Context.getCurrentContext(), integerSeven, new Object[] { decimalFourteen }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(-1)));
  }

  @Test
  public void testTrueCompareTrue() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue trueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = CompareMethods.compare(Context.getCurrentContext(), trueOne, new Object[] { trueTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(0)));
  }

  @Test
  public void testTrueCompareFalse() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue falseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = CompareMethods.compare(Context.getCurrentContext(), trueOne, new Object[] { falseTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));
  }

  @Test
  public void testAardvarkCompareBalloon() throws Exception {
    ScriptableValue aardvark = newValue(TextType.get().valueOf("Aardvark"));
    ScriptableValue balloon = newValue(TextType.get().valueOf("Balloon"));
    ScriptableValue result = CompareMethods.compare(Context.getCurrentContext(), aardvark, new Object[] { balloon }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(-1)));
  }

  @Test
  public void testZoomCompareAir() throws Exception {
    ScriptableValue zoom = newValue(TextType.get().valueOf("Zoom"));
    ScriptableValue air = newValue(TextType.get().valueOf("Air"));
    ScriptableValue result = CompareMethods.compare(Context.getCurrentContext(), zoom, new Object[] { air }, null);
    assertThat((Long) result.getValue().getValue(), greaterThan(0L));
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void testNullCompareAir() throws Exception {
    ScriptableValue nullOperand = newValue(TextType.get().nullValue());
    ScriptableValue air = newValue(TextType.get().valueOf("Air"));
    CompareMethods.compare(Context.getCurrentContext(), nullOperand, new Object[] { air }, null);
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void testAirCompareNull() throws Exception {
    ScriptableValue air = newValue(TextType.get().valueOf("Air"));
    ScriptableValue nullOperand = newValue(TextType.get().nullValue());
    CompareMethods.compare(Context.getCurrentContext(), air, new Object[] { nullOperand }, null);
  }

}

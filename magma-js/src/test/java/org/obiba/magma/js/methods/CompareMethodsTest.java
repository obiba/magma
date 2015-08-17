package org.obiba.magma.js.methods;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.MagmaContextFactory;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.fest.assertions.api.Assertions.assertThat;

public class CompareMethodsTest extends AbstractJsTest {

  @Test
  public void testIntegerCompareInteger() throws Exception {
    ScriptableValue integerSeven = newValue(IntegerType.get().valueOf(7));
    ScriptableValue integerZero = newValue(IntegerType.get().valueOf(0));
    ScriptableValue result = CompareMethods
        .compare(integerSeven, new Object[] { integerZero });
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));
  }

  @Test
  public void testIntegerCompareDecimal() throws Exception {
    ScriptableValue integerSeven = newValue(IntegerType.get().valueOf(7));
    ScriptableValue decimalFourteen = newValue(DecimalType.get().valueOf(14.0));
    ScriptableValue result = CompareMethods
        .compare(integerSeven, new Object[] { decimalFourteen });
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(-1));
  }

  @Test
  public void testDecimalCompareInteger() throws Exception {
    ScriptableValue integerSeven = newValue(IntegerType.get().valueOf(7));
    ScriptableValue decimalFourteen = newValue(DecimalType.get().valueOf(14.0));
    ScriptableValue result = CompareMethods
        .compare(decimalFourteen, new Object[] { integerSeven });
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));
  }

  @Test
  public void testDecimalCompareDecimal() throws Exception {
    ScriptableValue decimalSeven = newValue(DecimalType.get().valueOf(7.02));
    ScriptableValue decimalFourteen = newValue(DecimalType.get().valueOf(14.02));
    ScriptableValue result = CompareMethods
        .compare(decimalFourteen, new Object[] { decimalSeven });
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));
  }

  @Test
  public void testTrueCompareTrue() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue trueTwo = newValue(BooleanType.get().trueValue());
    ScriptableValue result = CompareMethods
        .compare(trueOne, new Object[] { trueTwo });
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(0));
  }

  @Test
  public void testTrueCompareFalse() throws Exception {
    ScriptableValue trueOne = newValue(BooleanType.get().trueValue());
    ScriptableValue falseTwo = newValue(BooleanType.get().falseValue());
    ScriptableValue result = CompareMethods
        .compare(trueOne, new Object[] { falseTwo });
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(1));
  }

  @Test
  public void testAardvarkCompareBalloon() throws Exception {
    ScriptableValue aardvark = newValue(TextType.get().valueOf("Aardvark"));
    ScriptableValue balloon = newValue(TextType.get().valueOf("Balloon"));
    ScriptableValue result = CompareMethods
        .compare(aardvark, new Object[] { balloon });
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(-1));
  }

  @Test
  public void testZoomCompareAir() throws Exception {
    ScriptableValue zoom = newValue(TextType.get().valueOf("Zoom"));
    ScriptableValue air = newValue(TextType.get().valueOf("Air"));
    ScriptableValue result = CompareMethods.compare(zoom, new Object[] { air });
    assertThat((Long) result.getValue().getValue()).isGreaterThan(0L);
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void testNullCompareAir() throws Exception {
    ScriptableValue nullOperand = newValue(TextType.get().nullValue());
    ScriptableValue air = newValue(TextType.get().valueOf("Air"));
    CompareMethods.compare(nullOperand, new Object[] { air });
  }

  @Test(expected = MagmaJsEvaluationRuntimeException.class)
  public void testAirCompareNull() throws Exception {
    ScriptableValue air = newValue(TextType.get().valueOf("Air"));
    ScriptableValue nullOperand = newValue(TextType.get().nullValue());
    CompareMethods.compare(air, new Object[] { nullOperand });
  }

  // compareNoCase

  @Test
  public void testCompareNoCaseBigAardvarkCompareSmallAardvark() throws Exception {
    ScriptableValue bigAardvark = newValue(TextType.get().valueOf("AARDVARK"));
    ScriptableValue smallAardvark = newValue(TextType.get().valueOf("aardvark"));
    ScriptableValue result = CompareMethods
        .compareNoCase(bigAardvark, new Object[] { smallAardvark });
    assertThat(result.getValue()).isEqualTo(IntegerType.get().valueOf(0));
  }

  @Test
  public void testCompareNoCaseBigAardvarkCompareCarrot() throws Exception {
    ScriptableValue bigAardvark = newValue(TextType.get().valueOf("AARDVARK"));
    ScriptableValue carrot = newValue(TextType.get().valueOf("carrot"));
    ScriptableValue result = CompareMethods
        .compareNoCase(bigAardvark, new Object[] { carrot });
    assertThat((Long) result.getValue().getValue()).isLessThan(0L);
  }
}

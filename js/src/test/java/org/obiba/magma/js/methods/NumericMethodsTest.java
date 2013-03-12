package org.obiba.magma.js.methods;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.Value;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.AbstractNumberType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class NumericMethodsTest extends AbstractJsTest {

  @Test
  public void test_integer_plus_integer() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .plus(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(3)));
  }

  @Test
  public void test_integer_plus_integers() throws Exception {
    ScriptableValue result = evaluate("plus(2,3,4)", IntegerType.get().valueOf(1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(10)));
  }

  @Test
  public void test_integer_plus_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods
        .plus(Context.getCurrentContext(), integerOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_null_plus_integer() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods
        .plus(Context.getCurrentContext(), nullOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_decimal_plus_null() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue nullTwo = newValue(DecimalType.get().nullValue());
    ScriptableValue result = NumericMethods
        .plus(Context.getCurrentContext(), decimalOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_null_plus_decimal() throws Exception {
    ScriptableValue nullOne = newValue(DecimalType.get().nullValue());
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue result = NumericMethods
        .plus(Context.getCurrentContext(), nullOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_decimal_plus_integer() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .plus(Context.getCurrentContext(), decimalOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  @Test
  public void test_integer_plus_decimal() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue result = NumericMethods
        .plus(Context.getCurrentContext(), integerOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  // minus

  @Test
  public void test_integer_minus_integer() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .minus(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(-1)));
  }

  @Test
  public void test_integer_minus_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods
        .minus(Context.getCurrentContext(), integerOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_null_minus_integer() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods
        .minus(Context.getCurrentContext(), nullOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_decimal_minus_integer() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .minus(Context.getCurrentContext(), decimalOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(-0.5)));
  }

  @Test
  public void test_integer_minus_decimal() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue result = NumericMethods
        .minus(Context.getCurrentContext(), integerOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(0.5)));
  }

  // multiply

  @Test
  public void test_integer_multiply_integer() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .multiply(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(2)));
  }

  @Test
  public void test_integer_multiply_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods
        .multiply(Context.getCurrentContext(), integerOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_null_multiply_integer() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods
        .multiply(Context.getCurrentContext(), nullOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_decimal_multiply_integer() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .multiply(Context.getCurrentContext(), decimalOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(3.0)));
  }

  @Test
  public void test_integer_multiply_decimal() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue result = NumericMethods
        .multiply(Context.getCurrentContext(), integerOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(3)));
  }

  @Test
  public void test_multiply_handlesUnits() throws Exception {
    ScriptableValue result = evaluate("multiply(newValue(5).unit('s'))", IntegerType.get().valueOf(5), "m");
    assertThat(result.getValue(), is(IntegerType.get().valueOf(25)));
    assertThat(result.getUnit(), is("m.s"));

    result = evaluate("multiply(newValue(5).unit('s'), newValue(5).unit('s'))", IntegerType.get().valueOf(1), "m");
    assertThat(result.getValue(), is(IntegerType.get().valueOf(25)));
    assertThat(result.getUnit(), is("m.s2"));
  }

  // div

  @Test
  public void test_integer_div_integer() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(7));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .div(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  @Test
  public void test_null_div_null_integer() throws Exception {
    ScriptableValue result = evaluate("div(null)", IntegerType.get().nullValue());
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_null_div_null_decimal() throws Exception {
    ScriptableValue result = evaluate("div(null)", DecimalType.get().nullValue());
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_integer_div_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods
        .div(Context.getCurrentContext(), integerOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_null_div_integer() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods
        .div(Context.getCurrentContext(), nullOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_decimal_div_null() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods
        .div(Context.getCurrentContext(), decimalOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_null_div_decimal() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue result = NumericMethods
        .div(Context.getCurrentContext(), nullOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_decimal_div_integer() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(7.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .div(Context.getCurrentContext(), decimalOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  @Test
  public void test_integer_div_decimal() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(7));
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue result = NumericMethods
        .div(Context.getCurrentContext(), integerOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  @Test
  public void test_integer_div_zero() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(7));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(0));
    ScriptableValue result = NumericMethods
        .div(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_div_handlesUnits() throws Exception {
    ScriptableValue result = evaluate("div(newValue(5).unit('s'))", IntegerType.get().valueOf(5), "m");
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));
    assertThat(result.getUnit(), is("m/s"));

    result = evaluate("div(newValue(5).unit('s'), newValue(5).unit('s'))", IntegerType.get().valueOf(25), "m");
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));
    assertThat(result.getUnit(), is("m/s2"));
  }

  // gt (>)

  @Test
  public void test_gt_null_one_gt_integer_two_is_null() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .gt(Context.getCurrentContext(), nullOne, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void test_gt_integer_one_gt_null_two_is_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods
        .gt(Context.getCurrentContext(), integerOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void test_gt_IntegerTwoGtIntegerThreeEqualsFalse() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integerThree = newValue(IntegerType.get().valueOf(3));
    ScriptableValue result = NumericMethods
        .gt(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integerThree }, null);
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));
  }

  @Test
  public void test_gt_DecimalTwoGtIntegerOneEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods
        .gt(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerOne }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void test_gt_integerGtNumbers() throws Exception {
    ScriptableValue result = evaluate("gt(2,3,4)", IntegerType.get().valueOf(1));
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));

    result = evaluate("gt(1.1)", IntegerType.get().valueOf(1));
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));

    result = evaluate("gt(-0.1)", IntegerType.get().valueOf(1));
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));

    result = evaluate("gt(2,3.5,4)", IntegerType.get().valueOf(5));
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  // ge (>=)

  @Test
  public void test_ge_null_one_ge_integer_two_is_null() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .ge(Context.getCurrentContext(), nullOne, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void test_ge_integer_one_ge_null_two_is_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods
        .ge(Context.getCurrentContext(), integerOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void test_ge_IntegerTwoGeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integer2 = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .ge(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integer2 }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void test_ge_DecimalTwoGeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .ge(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  // lt (<)

  @Test
  public void test_lt_null_one_lt_integer_two_is_null() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .lt(Context.getCurrentContext(), nullOne, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void test_lt_integer_one_lt_null_two_is_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods
        .lt(Context.getCurrentContext(), integerOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void test_lt_IntegerTwoLtIntegerThreeEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integerThree = newValue(IntegerType.get().valueOf(3));
    ScriptableValue result = NumericMethods
        .lt(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integerThree }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void test_lt_DecimalTwoLtIntegerOneEqualsFalse() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods
        .lt(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerOne }, null);
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));
  }

  // le (<=)

  @Test
  public void test_le_null_one_le_integer_two_is_null() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .le(Context.getCurrentContext(), nullOne, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void test_le_integer_one_le_null_two_is_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods
        .le(Context.getCurrentContext(), integerOne, new ScriptableValue[] { nullTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().nullValue()));
  }

  @Test
  public void test_le_IntegerTwoLeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integer2 = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .ge(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integer2 }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void test_le_DecimalTwoLeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods
        .ge(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  // abs

  @Test
  public void test_abs_evaluatesAbsoluteNullValue() throws Exception {
    ScriptableValue result = evaluate("abs()", IntegerType.get().nullValue());
    assertThat(result.getValue(), is(IntegerType.get().nullValue()));
  }

  @Test
  public void test_abs_evaluatesAbsoluteValue() throws Exception {
    ScriptableValue result = evaluate("abs()", DecimalType.get().valueOf(1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));

    result = evaluate("abs()", DecimalType.get().valueOf(-1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));

    result = evaluate("abs()", DecimalType.get().valueOf(0));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(0)));
  }

  @Test
  public void test_abs_acceptsIntegerType() throws Exception {
    ScriptableValue result = evaluate("abs()", IntegerType.get().valueOf(-1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));
  }

  @Test
  public void test_abs_handlesUnits() throws Exception {
    ScriptableValue result = evaluate("abs()", IntegerType.get().valueOf(-1), "m");
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));
    assertThat(result.getUnit(), is("m"));
  }

  // ln

  @Test
  public void test_ln_evaluatesNaturalLogarithmNullValue() throws Exception {
    ScriptableValue result = evaluate("ln()", DecimalType.get().nullValue());
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_ln_evaluatesNaturalLogarithm() throws Exception {
    ScriptableValue result = evaluate("ln()", DecimalType.get().valueOf(1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(0)));

    result = evaluate("ln()", DecimalType.get().valueOf(Math.E));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));

    result = evaluate("ln()", DecimalType.get().valueOf(42));
    assertThat(result.getValue(), is(DecimalType.get().valueOf(Math.log(42))));
  }

  @Test
  public void test_ln_acceptsIntegerType() throws Exception {
    ScriptableValue result = evaluate("ln()", IntegerType.get().valueOf(1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(0)));
  }

  // log, log(base)

  @Test
  public void test_log_evaluates_base_10_logarithm_null_value() throws Exception {
    ScriptableValue result = evaluate("log()", DecimalType.get().nullValue());
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_log_evaluatesBase10Logarithm() throws Exception {
    ScriptableValue result = evaluate("log()", DecimalType.get().valueOf(10));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));

    result = evaluate("log()", DecimalType.get().valueOf(100));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(2)));

    result = evaluate("log()", DecimalType.get().valueOf(1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(0)));

    result = evaluate("log()", DecimalType.get().valueOf(42));
    assertThat(result.getValue(), is(DecimalType.get().valueOf(Math.log10(42))));
  }

  @Test
  public void test_log_differentBaseThan10() throws Exception {
    ScriptableValue result = evaluate("log(2)", DecimalType.get().valueOf(2));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));

    result = evaluate("log(2)", DecimalType.get().valueOf(4));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(2)));

    result = evaluate("log(2)", DecimalType.get().valueOf(1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(0)));

    result = evaluate("log(2)", DecimalType.get().valueOf(1024));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(10)));
  }

  @Test
  public void test_log_acceptsIntegerType() throws Exception {
    ScriptableValue result = evaluate("log()", IntegerType.get().valueOf(1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(0)));
  }

  // pow

  @Test
  public void test_pow_evaluates_power_null_value() throws Exception {
    ScriptableValue result = evaluate("log()", DecimalType.get().nullValue());
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_pow_evaluatesPower() throws Exception {
    ScriptableValue result = evaluate("pow(1)", IntegerType.get().valueOf(1));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));

    result = evaluate("pow(2)", DecimalType.get().valueOf(Math.E));
    assertThat(result.getValue(), is(DecimalType.get().valueOf(Math.E * Math.E)));

    result = evaluate("pow(0)", IntegerType.get().valueOf(0));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));

    result = evaluate("pow(-1)", IntegerType.get().valueOf(10));
    assertThat(result.getValue(), is(DecimalType.get().valueOf(0.1)));
  }

  @Test
  public void test_pow_acceptsFractionalPowers() throws Exception {
    ScriptableValue result = evaluate("pow(1.1)", IntegerType.get().valueOf(5));
    assertThat(result.getValue(), is(DecimalType.get().valueOf(Math.pow(5, 1.1))));
  }

  @Test
  public void test_pow_handlesUnits() throws Exception {
    ScriptableValue result = evaluate("pow(2)", IntegerType.get().valueOf(5), "s");
    assertThat(result.getValue(), is(IntegerType.get().valueOf(25)));
    assertThat(result.getUnit(), is("s2"));

    result = evaluate("pow(-1)", IntegerType.get().valueOf(5), "m");
    assertThat(result.getValue(), is(DecimalType.get().valueOf(1 / 5d)));
    assertThat(result.getUnit(), is("1/m"));
  }

  // roots

  @Test
  public void test_root_evaluates_root_null_value() throws Exception {
    ScriptableValue result = evaluate("log()", DecimalType.get().nullValue());
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_root_evaluatesRoot() throws Exception {
    ScriptableValue result = evaluate("sqroot()", IntegerType.get().valueOf(4));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(2)));

    result = evaluate("root(2)", IntegerType.get().valueOf(4));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(2)));

    result = evaluate("cbroot()", IntegerType.get().valueOf(27));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(3)));

    result = evaluate("root(3)", IntegerType.get().valueOf(27));
    assertThat(result.getValue(), is(IntegerType.get().valueOf(3)));
  }

  @Test
  public void test_root_handlesUnits() throws Exception {
    ScriptableValue result = evaluate("sqroot()", IntegerType.get().valueOf(4), "m2/s2");
    assertThat(result.getValue(), is(IntegerType.get().valueOf(2)));
    assertThat(result.getUnit(), is("m/s"));
  }

  // group

  @Test
  public void test_group_IntegerWithBoundariesAndNoOutliers() {
    test_group_WithBoundariesAndNoOutliers(IntegerType.get());
  }

  @Test
  public void test_group_DecimalWithBoundariesAndNoOutliers() {
    test_group_WithBoundariesAndNoOutliers(DecimalType.get());
  }

  @Test
  public void test_group_NoBoundariesAndNoOutliers() {
    String group = "group([])";
    ScriptableValue value = evaluate(group, IntegerType.get().valueOf("1"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf("1")));
  }

  @Test
  public void test_group_NullValue() {
    String group = "group([0,10,20])";
    ScriptableValue value = evaluate(group, IntegerType.get().nullValue());
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().nullValue()));
  }

  @Test
  public void test_group_NullSequence() {
    String group = "group([0,10,20])";
    ScriptableValue value = evaluate(group, IntegerType.get().sequenceOf((Iterable<Value>) null));
    assertThat(value.getValue().isNull(), is(true));
    assertThat(value.getValue().isSequence(), is(true));
    assertThat(value.getValue().asSequence(), is(TextType.get().nullSequence()));
  }

  private void test_group_WithBoundariesAndNoOutliers(AbstractNumberType numberType) {
    String group = "group([0,5,10,15,20])";
    ScriptableValue value = evaluate(group, numberType.valueOf("-1"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf("-0")));

    value = evaluate(group, numberType.valueOf("1"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf("0-5")));

    value = evaluate(group, numberType.valueOf("10"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf("10-15")));

    value = evaluate(group, numberType.valueOf("20"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf("20+")));

    value = evaluate(group, numberType.valueOf("21"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf("20+")));
  }

  @Test
  public void test_group_IntegerWithBoundariesAndOutliers() {
    test_group_WithBoundariesAndOutliers(IntegerType.get());
  }

  @Test
  public void test_group_DecimalWithBoundariesAndOutliers() {
    test_group_WithBoundariesAndOutliers(DecimalType.get());
  }

  private void test_group_WithBoundariesAndOutliers(AbstractNumberType numberType) {
    ScriptableValue value = evaluate("group([5,10,15,20],[11,20])", numberType.valueOf("11"));
    assertThat(value, notNullValue());
    assertThat(value.getValue(), is(TextType.get().valueOf(numberType.valueOf("11"))));
  }
}

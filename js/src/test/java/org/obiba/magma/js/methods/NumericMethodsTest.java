package org.obiba.magma.js.methods;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;

public class NumericMethodsTest extends AbstractJsTest {

  @Test
  public void test_integer_plus_integer() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.plus(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
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
    ScriptableValue result = NumericMethods.plus(Context.getCurrentContext(), integerOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));
  }

  @Test
  public void test_null_plus_integer() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods.plus(Context.getCurrentContext(), nullOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));
  }

  @Test
  public void test_decimal_plus_integer() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.plus(Context.getCurrentContext(), decimalOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  @Test
  public void test_integer_plus_decimal() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue result = NumericMethods.plus(Context.getCurrentContext(), integerOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  // minus

  @Test
  public void test_integer_minus_integer() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.minus(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(-1)));
  }

  @Test
  public void test_integer_minus_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods.minus(Context.getCurrentContext(), integerOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(1)));
  }

  @Test
  public void test_null_minus_integer() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods.minus(Context.getCurrentContext(), nullOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(-1)));
  }

  @Test
  public void test_decimal_minus_integer() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.minus(Context.getCurrentContext(), decimalOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(-0.5)));
  }

  @Test
  public void test_integer_minus_decimal() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue result = NumericMethods.minus(Context.getCurrentContext(), integerOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(0.5)));
  }

  // multiply

  @Test
  public void test_integer_multiply_integer() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.multiply(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(2)));
  }

  @Test
  public void test_integer_multiply_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods.multiply(Context.getCurrentContext(), integerOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(0)));
  }

  @Test
  public void test_null_multiply_integer() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods.multiply(Context.getCurrentContext(), nullOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(IntegerType.get().valueOf(0)));
  }

  @Test
  public void test_decimal_multiply_integer() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.multiply(Context.getCurrentContext(), decimalOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.0)));
  }

  @Test
  public void test_integer_multiply_decimal() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(2));
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(1.5));
    ScriptableValue result = NumericMethods.multiply(Context.getCurrentContext(), integerOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.0)));
  }

  // div

  @Test
  public void test_integer_div_integer() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(7));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.div(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  @Test
  public void test_integer_div_null() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue nullTwo = newValue(IntegerType.get().nullValue());
    ScriptableValue result = NumericMethods.div(Context.getCurrentContext(), integerOne, new Object[] { nullTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  @Test
  public void test_null_div_integer() throws Exception {
    ScriptableValue nullOne = newValue(IntegerType.get().nullValue());
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods.div(Context.getCurrentContext(), nullOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(0)));
  }

  @Test
  public void test_decimal_div_integer() throws Exception {
    ScriptableValue decimalOne = newValue(DecimalType.get().valueOf(7.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.div(Context.getCurrentContext(), decimalOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  @Test
  public void test_integer_div_decimal() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(7));
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue result = NumericMethods.div(Context.getCurrentContext(), integerOne, new Object[] { decimalTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().valueOf(3.5)));
  }

  @Test
  public void test_integer_div_zero() throws Exception {
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(7));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(0));
    ScriptableValue result = NumericMethods.div(Context.getCurrentContext(), integerOne, new Object[] { integerTwo }, null);
    assertThat(result.getValue(), is(DecimalType.get().nullValue()));
  }

  // gt (>)

  @Test
  public void test_gt_IntegerTwoGtIntegerThreeEqualsFalse() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integerThree = newValue(IntegerType.get().valueOf(3));
    ScriptableValue result = NumericMethods.gt(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integerThree }, null);
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));
  }

  @Test
  public void test_gt_DecimalTwoGtIntegerOneEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods.gt(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerOne }, null);
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
  public void test_ge_IntegerTwoGeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integer2 = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.ge(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integer2 }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void test_ge_DecimalTwoGeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.ge(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  // lt (<)

  @Test
  public void test_lt_IntegerTwoLtIntegerThreeEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integerThree = newValue(IntegerType.get().valueOf(3));
    ScriptableValue result = NumericMethods.lt(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integerThree }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void test_lt_DecimalTwoLtIntegerOneEqualsFalse() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerOne = newValue(IntegerType.get().valueOf(1));
    ScriptableValue result = NumericMethods.lt(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerOne }, null);
    assertThat(result.getValue(), is(BooleanType.get().falseValue()));
  }

  // le (<=)

  @Test
  public void test_le_IntegerTwoLeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue integer2 = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.ge(Context.getCurrentContext(), integerTwo, new ScriptableValue[] { integer2 }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

  @Test
  public void test_le_DecimalTwoLeIntegerTwoEqualsTrue() throws Exception {
    ScriptableValue decimalTwo = newValue(DecimalType.get().valueOf(2.0));
    ScriptableValue integerTwo = newValue(IntegerType.get().valueOf(2));
    ScriptableValue result = NumericMethods.ge(Context.getCurrentContext(), decimalTwo, new ScriptableValue[] { integerTwo }, null);
    assertThat(result.getValue(), is(BooleanType.get().trueValue()));
  }

}

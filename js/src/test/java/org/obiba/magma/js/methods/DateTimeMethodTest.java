package org.obiba.magma.js.methods;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.AbstractJsTest;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.IntegerType;

public class DateTimeMethodTest extends AbstractJsTest {

  @Test
  public void test_year_acceptsDateTime() {
    testCalendarFieldMethod("year()", Calendar.YEAR, DateTimeType.get());
  }

  @Test
  public void test_year_acceptsDate() {
    testCalendarFieldMethod("year()", Calendar.YEAR, DateType.get());
  }

  @Test
  public void test_month_acceptsDateTime() {
    testCalendarFieldMethod("month()", Calendar.MONTH, DateTimeType.get());
  }

  @Test
  public void test_month_acceptsDate() {
    testCalendarFieldMethod("month()", Calendar.MONTH, DateType.get());
  }

  @Test
  public void test_dayOfMonth_acceptsDateTime() {
    testCalendarFieldMethod("dayOfMonth()", Calendar.DAY_OF_MONTH, DateTimeType.get());
  }

  @Test
  public void test_dayOfMonth_acceptsDate() {
    testCalendarFieldMethod("dayOfMonth()", Calendar.DAY_OF_MONTH, DateType.get());
  }

  @Test
  public void test_dayOfWeek_acceptsDateTime() {
    testCalendarFieldMethod("dayOfWeek()", Calendar.DAY_OF_WEEK, DateTimeType.get());
  }

  @Test
  public void test_dayOfWeek_acceptsDate() {
    testCalendarFieldMethod("dayOfWeek()", Calendar.DAY_OF_WEEK, DateType.get());
  }

  @Test
  public void test_dayOfYear_acceptsDateTime() {
    testCalendarFieldMethod("dayOfYear()", Calendar.DAY_OF_YEAR, DateTimeType.get());
  }

  @Test
  public void test_dayOfYear_acceptsDate() {
    testCalendarFieldMethod("dayOfYear()", Calendar.DAY_OF_YEAR, DateType.get());
  }

  @Test
  public void test_weekOfYear_acceptsDateTime() {
    testCalendarFieldMethod("weekOfYear()", Calendar.WEEK_OF_YEAR, DateTimeType.get());
  }

  @Test
  public void test_weekOfYear_acceptsDate() {
    testCalendarFieldMethod("weekOfYear()", Calendar.WEEK_OF_YEAR, DateType.get());
  }

  @Test
  public void test_weekOfMonth_acceptsDateTime() {
    testCalendarFieldMethod("weekOfMonth()", Calendar.WEEK_OF_MONTH, DateTimeType.get());
  }

  @Test
  public void test_weekOfMonth_acceptsDate() {
    testCalendarFieldMethod("weekOfMonth()", Calendar.WEEK_OF_MONTH, DateType.get());
  }

  @Test
  public void testAfterNullArgumentReturnsNull() throws Exception {
    ScriptableValue now = newValue(DateTimeType.get().valueOf(new Date()));
    ScriptableValue nullDate = newValue(DateTimeType.get().nullValue());
    ScriptableValue result = (ScriptableValue) DateTimeMethods.after(Context.getCurrentContext(), now, new ScriptableValue[] { nullDate }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().nullValue(), result.getValue());
  }

  @Test
  public void testAfterNullCallerReturnsNull() throws Exception {
    ScriptableValue now = newValue(DateTimeType.get().valueOf(new Date()));
    ScriptableValue nullDate = newValue(DateTimeType.get().nullValue());
    ScriptableValue result = (ScriptableValue) DateTimeMethods.after(Context.getCurrentContext(), nullDate, new ScriptableValue[] { now }, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(BooleanType.get().nullValue(), result.getValue());
  }

  /**
   * Executes {@code script} with {@code testValue} as its scope and asserts that the returned value is
   * 
   * @param script
   * @param field
   * @param testValue
   */
  private void testCalendarFieldMethod(String script, int field, ValueType testType) {
    Calendar testValue = Calendar.getInstance();
    ScriptableValue result = evaluate(script, testType.valueOf(testValue));
    assertIntegerResult(result, testValue.get(field));
  }

  private void assertIntegerResult(ScriptableValue result, int expectedValue) {
    assertThat(result, notNullValue());
    assertThat(result.getValue(), notNullValue());
    assertThat(result.getValueType(), is((ValueType) IntegerType.get()));
    assertThat((Long) result.getValue().getValue(), is((long) expectedValue));
  }
}

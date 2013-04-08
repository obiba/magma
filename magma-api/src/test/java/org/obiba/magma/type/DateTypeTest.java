package org.obiba.magma.type;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;

import com.google.common.collect.ImmutableList;

import junit.framework.Assert;

public class DateTypeTest extends BaseValueTypeTest {

  @Override
  DateType getValueType() {
    return DateType.get();
  }

  @Override
  Object getObjectForType() {
    return new MagmaDate(new Date());
  }

  @Override
  boolean isDateTime() {
    return true;
  }

  @Override
  boolean isNumeric() {
    return false;
  }

  @Override
  Iterable<Class<?>> validClasses() {
    return ImmutableList.<Class<?>>of(MagmaDate.class);
  }

  @Test
  public void testValueOfSqlDateInstance() {
    DateType dt = DateType.get();

    // MAGMA-166
    // Assert.assertTrue(dt.acceptsJavaClass(java.sql.Date.class));

    Date dateValue = new Date();
    Value value = dt.valueOf(new java.sql.Date(dateValue.getTime()));
    Assert.assertEquals(new MagmaDate(dateValue), value.getValue());

    // Make sure the type was normalized
    Assert.assertTrue(value.getValue().getClass().equals(dt.getJavaClass()));
  }

  @Test
  public void testValueOfSqlTimestampInstance() {
    DateType dt = DateType.get();

    // MAGMA-166
    // Assert.assertTrue(dt.acceptsJavaClass(Timestamp.class));

    Date dateValue = new Date();
    Value value = dt.valueOf(new Timestamp(dateValue.getTime()));
    Assert.assertEquals(new MagmaDate(dateValue), value.getValue());

    // Make sure the type was normalized
    Assert.assertTrue(value.getValue().getClass().equals(dt.getJavaClass()));
  }

  @Test
  public void testValueOfCalendarInstance() {
    DateTimeType dt = DateTimeType.get();

    // MAGMA-166
    // Assert.assertTrue(dt.acceptsJavaClass(Calendar.class));
    // Assert.assertTrue(dt.acceptsJavaClass(GregorianCalendar.class));

    Calendar calendar = GregorianCalendar.getInstance();
    Date dateValue = calendar.getTime();
    Value value = dt.valueOf(calendar);
    Assert.assertEquals(dateValue, value.getValue());
  }

  @Test
  public void testValueOfISODateFormatString() {
    assertValueOfUsingDateFormat("yyyy-MM-dd");
  }

  @Test
  public void testValueOfDashDateFormatString() {
    assertValueOfUsingDateFormat("dd-MM-yyyy");
  }

  @Test
  public void testValueOfSlashDateFormatString1() {
    assertValueOfUsingDateFormat("dd/MM/yyyy");
  }

  @Test
  public void testValueOfSlashDateFormatString2() {
    assertValueOfUsingDateFormat("yyyy/MM/dd");
  }

  @Test
  public void testValueOfDotDateFormatString1() {
    assertValueOfUsingDateFormat("dd.MM.yyyy");
  }

  @Test
  public void testValueOfDotDateFormatString2() {
    assertValueOfUsingDateFormat("yyyy.MM.dd");
  }

  @Test
  public void testValueOfSpaceDateFormatString1() {
    assertValueOfUsingDateFormat("dd MM yyyy");
  }

  @Test
  public void testValueOfSpaceDateFormatString2() {
    assertValueOfUsingDateFormat("yyyy MM dd");
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_valueOf_invalidFormat() {
    getValueType().valueOf("2011_11_10");
  }

  @Test
  public void test_toString_nullValueReturnsNull() {
    String s = getValueType().toString((Object) null);
    Assert.assertNull(s);
  }

  @Test
  public void test_now_returnsNewDate() {
    Value now = getValueType().now();
    Assert.assertEquals(new MagmaDate(new Date()), now.getValue());
  }

  private void assertValueOfUsingDateFormat(String dateFormat) {
    DateType dt = DateType.get();
    Date dateValue = new Date();
    String dateStr = new SimpleDateFormat(dateFormat).format(dateValue);
    Value value = dt.valueOf(dateStr);
    Assert.assertEquals(new MagmaDate(dateValue), value.getValue());
  }
}

package org.obiba.magma.type;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;

public class DateTypeTest {

  @BeforeClass
  public static void startYourEngine() {
    new MagmaEngine();

  }

  @Test
  public void testValueOfDateInstance() {
    DateType dt = DateType.get();

    Assert.assertTrue(dt.acceptsJavaClass(Date.class));

    Date dateValue;
    Value value = dt.valueOf(dateValue = new Date());
    Assert.assertEquals(dateValue, value.getValue());
  }

  @Test
  public void testValueOfSqlDateInstance() {
    DateType dt = DateType.get();

    Assert.assertTrue(dt.acceptsJavaClass(java.sql.Date.class));

    Date dateValue = new Date();
    Value value = dt.valueOf(new java.sql.Date(dateValue.getTime()));
    Assert.assertEquals(dateValue, value.getValue());

    // Make sure the type was normalized
    Assert.assertTrue(value.getValue().getClass().equals(dt.getJavaClass()));
  }

  @Test
  public void testValueOfSqlTimestampInstance() {
    DateType dt = DateType.get();

    Assert.assertTrue(dt.acceptsJavaClass(Timestamp.class));

    Date dateValue = new Date();
    Value value = dt.valueOf(new Timestamp(dateValue.getTime()));
    Assert.assertEquals(dateValue, value.getValue());

    // Make sure the type was normalized
    Assert.assertTrue(value.getValue().getClass().equals(dt.getJavaClass()));
  }

  @Test
  public void testValueOfCalendarInstance() {
    DateType dt = DateType.get();

    Assert.assertTrue(dt.acceptsJavaClass(Calendar.class));
    Assert.assertTrue(dt.acceptsJavaClass(GregorianCalendar.class));

    Calendar calendar = GregorianCalendar.getInstance();
    Date dateValue = calendar.getTime();
    Value value = dt.valueOf(calendar);
    Assert.assertEquals(dateValue, value.getValue());
  }

  @Test
  public void testValueOfISODateFormatString() {
    assertValueOfUsingDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  }

  @Test
  public void testValueOfDateFormatString() {
    assertValueOfUsingDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz");
  }

  private void assertValueOfUsingDateFormat(String dateFormat) {
    DateType dt = DateType.get();
    Date dateValue = new Date();
    Value value = dt.valueOf(new SimpleDateFormat(dateFormat).format(dateValue));
    Assert.assertEquals(dateValue, value.getValue());
  }
}

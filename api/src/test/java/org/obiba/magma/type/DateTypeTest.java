package org.obiba.magma.type;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

public class DateTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return DateType.get();
  }

  @Override
  Object getObjectForType() {
    return new MagmaDate(new Date());
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

  private void assertValueOfUsingDateFormat(String dateFormat) {
    DateType dt = DateType.get();
    Date dateValue = new Date();
    Value value = dt.valueOf(new SimpleDateFormat(dateFormat).format(dateValue));
    Assert.assertEquals(new MagmaDate(dateValue), value.getValue());
  }
}

/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

public class DateTimeTypeTest extends BaseValueTypeTest {

  @Override
  ValueType getValueType() {
    return DateTimeType.get();
  }

  @Override
  Object getObjectForType() {
    return new Date();
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
    return ImmutableList.<Class<?>>of(Date.class, Timestamp.class, Calendar.class);
  }

  @Test
  public void testValueOfSqlDateInstance() {
    DateTimeType dt = DateTimeType.get();

    assertThat(dt.acceptsJavaClass(java.sql.Date.class)).isTrue();

    Date dateValue = new Date();
    Value value = dt.valueOf(new java.sql.Date(dateValue.getTime()));
    assertThat(dateValue).isEqualTo((Date) value.getValue());

    // Make sure the type was normalized
    assertThat(value.getValue().getClass().equals(dt.getJavaClass())).isTrue();
  }

  @Test
  public void testValueOfSqlTimestampInstance() {
    DateTimeType dt = DateTimeType.get();

    assertThat(dt.acceptsJavaClass(Timestamp.class)).isTrue();

    Date dateValue = new Date();
    Value value = dt.valueOf(new Timestamp(dateValue.getTime()));
    assertThat(dateValue).isEqualTo((Date) value.getValue());

    // Make sure the type was normalized
    assertThat(value.getValue().getClass().equals(dt.getJavaClass())).isTrue();
  }

  @Test
  public void testValueOfCalendarInstance() {
    DateTimeType dt = DateTimeType.get();

    assertThat(dt.acceptsJavaClass(Calendar.class)).isTrue();
    assertThat(dt.acceptsJavaClass(GregorianCalendar.class)).isTrue();

    Calendar calendar = GregorianCalendar.getInstance();
    Date dateValue = calendar.getTime();
    Value value = dt.valueOf(calendar);
    assertThat(dateValue).isEqualTo((Date) value.getValue());
  }

  @Test
  public void test_valueOfISODateFormatString() {
    assertValueOfUsingDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  }

  @Test
  public void test_valueOfISODateFormatNoMillisecondsString() {
    // seconds precision
    assertValueOfUsingDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", 1000);
  }

  @Test
  public void test_valueOfISODateFormatNoSecondsString() {
    // minutes precision
    assertValueOfUsingDateFormat("yyyy-MM-dd'T'HH:mmZ", 1000 * 60);
  }

  @Test
  public void test_valueOfIncorrectISODateFormatString() {
    assertValueOfUsingDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz");
  }

  @Test
  public void test_valueOfNoTimeZoneDateFormatString() {
    Calendar expected = Calendar.getInstance();
    expected.clear();
    expected.set(2011, Calendar.JANUARY, 25, 14, 30, 47);
    Value value = DateTimeType.get().valueOf("2011-01-25 14:30:47");
    assertThat(new Date(expected.getTimeInMillis())).isEqualTo((Date) value.getValue());
  }

  @Test
  public void test_valueOfSimpleDateString() {
    Calendar expected = Calendar.getInstance();
    expected.clear();
    expected.set(2011, Calendar.JANUARY, 25, 0, 0, 0);
    Value value = DateTimeType.get().valueOf("2011-01-25");
    assertThat(new Date(expected.getTimeInMillis())).isEqualTo((Date) value.getValue());
  }

  @Test
  public void test_valueOfNoTimeZoneDateFormatStringNoSeconds() {
    Calendar expected = Calendar.getInstance();
    expected.clear();
    expected.set(2011, Calendar.JANUARY, 25, 14, 30);
    Value value = DateTimeType.get().valueOf("2011-01-25 14:30");
    assertThat(new Date(expected.getTimeInMillis())).isEqualTo((Date) value.getValue());
  }

  @Test
  public void test_valueOfNoTimeZoneSlashDateFormatStringNoSeconds() {
    Calendar expected = Calendar.getInstance();
    expected.clear();
    expected.set(2011, Calendar.JANUARY, 25, 14, 30);
    Value value = DateTimeType.get().valueOf("2011/01/25 14:30");
    assertThat((Date) value.getValue()).isEqualTo(new Date(expected.getTimeInMillis()));
  }

  @Test
  public void test_valueOfThatIncludesZuluTimezone() {
    Calendar expected = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    expected.clear();
    expected.set(2011, Calendar.JANUARY, 25, 14, 30, 47);
    Value value = DateTimeType.get().valueOf("2011-01-25T14:30:47Z");
    assertThat(new Date(expected.getTimeInMillis())).isEqualTo((Date) value.getValue());
  }

  @Test
  public void test_iso8601TimeZone() {
    Calendar expected = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    expected.clear();
    expected.set(2011, Calendar.JANUARY, 25, 14, 30, 47);
    Value value = DateTimeType.get().valueOf("2011-01-25T14:30:47+00:00");
    assertThat(new Date(expected.getTimeInMillis())).isEqualTo((Date) value.getValue());
  }

  private void assertValueOfUsingDateFormat(String dateFormat) {
    assertValueOfUsingDateFormat(dateFormat, 0);
  }

  private void assertValueOfUsingDateFormat(String dateFormat, int precision) {
    Date dateValue = new Date();
    Value value = getValueType().valueOf(new SimpleDateFormat(dateFormat).format(dateValue));
    if(precision == 0) {
      assertThat(dateValue).isEqualTo((Date) value.getValue());
    } else {
      // asserts that times are equivalent within "precision" from each other
      assertThat(Math.abs(dateValue.getTime() - ((Date) value.getValue()).getTime()) < precision).isTrue();
    }
  }
}

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

import org.junit.Test;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

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
    // assertThat((dt.acceptsJavaClass(java.sql.Date.class))).isTrue();

    Date dateValue = new Date();
    Value value = dt.valueOf(new java.sql.Date(dateValue.getTime()));
    assertThat(new MagmaDate(dateValue)).isEqualTo((MagmaDate) value.getValue());

    // Make sure the type was normalized
    assertThat(value.getValue()).isInstanceOf(dt.getJavaClass());
  }

  @Test
  public void testValueOfSqlTimestampInstance() {
    DateType dt = DateType.get();

    // MAGMA-166
    // assertThat((dt.acceptsJavaClass(Timestamp.class))).isTrue();

    Date dateValue = new Date();
    Value value = dt.valueOf(new Timestamp(dateValue.getTime()));
    assertThat(new MagmaDate(dateValue)).isEqualTo((MagmaDate) value.getValue());

    // Make sure the type was normalized
    assertThat(value.getValue()).isInstanceOf(dt.getJavaClass());
  }

  @Test
  public void testValueOfCalendarInstance() {
    DateTimeType dt = DateTimeType.get();

    // MAGMA-166
    // assertThat((dt.acceptsJavaClass(Calendar.class))).isTrue();
    // assertThat((dt.acceptsJavaClass(GregorianCalendar.class))).isTrue();

    Calendar calendar = GregorianCalendar.getInstance();
    Date dateValue = calendar.getTime();
    Value value = dt.valueOf(calendar);
    assertThat(dateValue).isEqualTo((Date) value.getValue());
  }

  @Test
  public void testValueOfDateValue() {
    Value val = DateType.get().valueOf(new Date());
    assertThat(val).isEqualTo(DateType.get().valueOf(val));
  }

  @Test
  public void testValueOfTextValue() {
    Date now = new Date();
    Value val = TextType.get().valueOf(new SimpleDateFormat("yyyy-MM-dd").format(now));
    assertThat(val.toString()).isEqualTo(DateType.get().valueOf(val).toString());
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
    assertThat(s).isNull();
  }

  @Test
  public void test_now_returnsNewDate() {
    assertThat(getValueType().now().getValue()).isEqualTo(new MagmaDate(new Date()));
  }

  private void assertValueOfUsingDateFormat(String dateFormat) {
    DateType dt = DateType.get();
    Date dateValue = new Date();
    String dateStr = new SimpleDateFormat(dateFormat).format(dateValue);
    Value value = dt.valueOf(dateStr);
    assertThat(value.getValue()).isEqualTo(new MagmaDate(dateValue));
  }
}

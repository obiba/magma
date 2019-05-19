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

import java.util.Date;

import org.junit.Test;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.MagmaTest;
import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;

import static org.fest.assertions.api.Assertions.assertThat;

public class DateTimeValueConverterTest extends MagmaTest {

  @Test
  public void test_convert_handlesDateTimeToDate() {
    ValueConverter converter = new DatetimeValueConverter();
    Date dateValue = new Date();
    Value value = converter.convert(DateTimeType.get().valueOf(dateValue), DateType.get());
    assertThat((MagmaDate) value.getValue()).isEqualTo(new MagmaDate(dateValue));
  }

  @Test
  public void test_convert_handlesMagmaDateToDate() {
    ValueConverter converter = new DatetimeValueConverter();
    MagmaDate dateValue = new MagmaDate(new Date());
    Value value = converter.convert(DateType.get().valueOf(dateValue), DateType.get());
    assertThat((MagmaDate) value.getValue()).isEqualTo(dateValue);
  }

  @Test
  public void test_convert_handlesMagmaDateToDateTime() {
    ValueConverter converter = new DatetimeValueConverter();
    MagmaDate dateValue = new MagmaDate(new Date());
    Value value = converter.convert(DateType.get().valueOf(dateValue), DateTimeType.get());
    assertThat((Date) value.getValue()).isEqualTo(dateValue.asDate());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void test_convert_handlesDateToDateTime() {
    ValueConverter converter = new DatetimeValueConverter();

    Date dateTimeValue = new Date();
    // The date only
    Date dateValue = new Date(dateTimeValue.getYear(), dateTimeValue.getMonth(), dateTimeValue.getDate());

    Value value = converter.convert(DateType.get().valueOf(new MagmaDate(dateTimeValue)), DateTimeType.get());
    // Anything below the day should have been truncated
    assertThat((Date) value.getValue()).isEqualTo(dateValue);
  }

}

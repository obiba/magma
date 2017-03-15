package org.obiba.magma.type;

import java.util.Date;

import org.junit.Test;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.ValueConverter;
import org.obiba.magma.test.AbstractMagmaTest;

import static org.fest.assertions.api.Assertions.assertThat;

public class DateTimeValueConverterTest extends AbstractMagmaTest {

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

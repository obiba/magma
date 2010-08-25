package org.obiba.magma.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.test.AbstractMagmaTest;

public class DateTimeValueConverterTest extends AbstractMagmaTest {

  @Test
  public void test_convert_handlesDateTimeToDate() {
    DatetimeValueConverter converter = new DatetimeValueConverter();
    Date dateValue = new Date();
    Value value = converter.convert(DateTimeType.get().valueOf(dateValue), DateType.get());
    assertThat((MagmaDate) value.getValue(), is(new MagmaDate(dateValue)));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void test_convert_handlesDateToDateTime() {
    DatetimeValueConverter converter = new DatetimeValueConverter();

    Date dateTimeValue = new Date();
    // The date only
    Date dateValue = new Date(dateTimeValue.getYear(), dateTimeValue.getMonth(), dateTimeValue.getDate());

    Value value = converter.convert(DateType.get().valueOf(new MagmaDate(dateTimeValue)), DateTimeType.get());
    // Anything below the day should have been truncated
    assertThat((Date) value.getValue(), is(dateValue));
  }

}

/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.spss.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class SpssDateValueConverters {

  private SpssDateValueConverters() {}

  private static abstract class AbstractDateValueConverter implements SpssValueConverter {

    public abstract String getFormat(String value) throws SpssValueConversionException;

    protected SpssValueConversionException createException(String value, String... formats) {
      StringBuilder message = new StringBuilder("Failed to convert date value. Expected SPSS date formats are:");
      for (String format : formats) message.append(" ").append(format);
      return new SpssValueConversionException(message.toString(), value);
    }

    @Override
    public String convert(String value) throws SpssValueConversionException {
      SimpleDateFormat source = new SimpleDateFormat(getFormat(value));

      try {
        Date date = source.parse(value);
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
      } catch(ParseException e) {
        throw new SpssValueConversionException("Failed to convert date value.", e, value);
      }

    }
  }

  public static class ADateValueConverter extends AbstractDateValueConverter {
    private static final String SHORT_ADATE = "MM/dd/yy";

    private static final String LONG_ADATE = "MM/dd/yyyy";

    @Override
    public String getFormat(String value) throws SpssValueConversionException {
      int length = value.length();

      if(length == SHORT_ADATE.length()) {
        return SHORT_ADATE;
      } else if(length == LONG_ADATE.length()) {
        return LONG_ADATE;
      }

      throw createException(SHORT_ADATE, LONG_ADATE);
    }
  }

  public static class DateValueConverter extends AbstractDateValueConverter {
    private static final String SHORT_DATE = "dd-MMM-yy";

    private static final String LONG_DATE = "dd-MMM-yyyy";

    @Override
    public String getFormat(String value) throws SpssValueConversionException {
      int length = value.length();

      if(length == SHORT_DATE.length()) {
        return SHORT_DATE;
      } else if(length == LONG_DATE.length()) {
        return LONG_DATE;
      }

      throw createException(SHORT_DATE, LONG_DATE);
    }
  }

  public static class DateTimeValueConverter extends AbstractDateValueConverter {
    private static final String SHORT_DATETIME = "dd-MMM-yyyy hh:mm";

    private static final String LONG_DATETIME = "dd-MMM-yyyy hh:mm:ss";

    @Override
    public String getFormat(String value) throws SpssValueConversionException {
      int length = value.length();

      if(length == SHORT_DATETIME.length()) {
        return SHORT_DATETIME;
      } else if(length == LONG_DATETIME.length()) {
        return LONG_DATETIME;
      }

      throw createException(SHORT_DATETIME, LONG_DATETIME);
    }
  }

}

/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.type;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaDate;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.support.ValueComparator;

public class DateTimeType extends AbstractValueType {

  private static final long serialVersionUID = -149385659514790222L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<DateTimeType> instance;

  /**
   * Preferred date time format.
   */
  private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

  /**
   * These are used to support other common date time formats.
   */
  private final SimpleDateFormat[] dateFormats = new SimpleDateFormat[] { //
      ISO_8601, //
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"), //
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX"), //
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"), //
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"), //
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ"), //
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz"), //
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), //
      new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"), //
      new SimpleDateFormat("yyyy.MM.dd HH:mm:ss"), //
      new SimpleDateFormat("yyyy MM dd HH:mm:ss"), //
      new SimpleDateFormat("yyyy-MM-dd HH:mm"), //
      new SimpleDateFormat("yyyy/MM/dd HH:mm"), //
      new SimpleDateFormat("yyyy.MM.dd HH:mm"), //
      new SimpleDateFormat("yyyy MM dd HH:mm") };

  private String dateFormatPatterns = "";

  private DateTimeType() {
    // Force strict year parsing, otherwise 2 digits can be interpreted as a 4 digits year...
    for(SimpleDateFormat format : dateFormats) {
      format.setLenient(false);
      if(dateFormatPatterns.isEmpty()) {
        dateFormatPatterns = "'" + format.toPattern() + "'";
      } else {
        dateFormatPatterns += ", '" + format.toPattern() + "'";
      }
    }
  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @NotNull
  public static DateTimeType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new DateTimeType());
    }
    return instance.get();
  }

  @Override
  public boolean isDateTime() {
    return true;
  }

  @Override
  public boolean isNumeric() {
    return false;
  }

  @Override
  public Class<?> getJavaClass() {
    return Date.class;
  }

  @NotNull
  @Override
  public String getName() {
    return "datetime";
  }

  @Override
  public boolean acceptsJavaClass(@NotNull Class<?> clazz) {
    return Date.class.isAssignableFrom(clazz) || java.sql.Date.class.isAssignableFrom(clazz) ||
        Timestamp.class.isAssignableFrom(clazz) || Calendar.class.isAssignableFrom(clazz);
  }

  @Override
  public String toString(Object object) {
    // DateFormat is not thread safe
    synchronized(ISO_8601) {
      return ISO_8601.format((Date) object);
    }
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }
    String dateToParse = string;
    if(string.endsWith("Z")) {
      // Java before 7 does not support the 'Zulu' timezone (Z). Replace it with a SimpleDateFormat-friendly timezone
      dateToParse = string.replaceFirst("Z$", "UTC");
    }

    for(SimpleDateFormat format : dateFormats) {
      try {
        return parseDate(format, dateToParse);
      } catch(ParseException e) {
        // ignored
      }
    }
    throw new MagmaRuntimeException(
        "Cannot parse date from string value '" + string + "'. Expected format is one of " + dateFormatPatterns);
  }

  private Value parseDate(SimpleDateFormat format, String string) throws ParseException {
    // DateFormat is not thread safe
    synchronized(format) {
      return Factory.newValue(this, format.parse(string));
    }
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null) {
      return nullValue();
    }
    Class<?> type = object.getClass();
    if(type.equals(Date.class)) {
      return Factory.newValue(this, (Serializable) object);
    }
    if(type.equals(MagmaDate.class)) {
      return Factory.newValue(this, ((MagmaDate) object).asDate());
    }
    if(Date.class.isAssignableFrom(type)) {
      return Factory.newValue(this, new Date(((Date) object).getTime()));
    }
    if(Calendar.class.isAssignableFrom(type)) {
      Calendar c = (Calendar) object;
      return Factory.newValue(this, c.getTime());
    }
    if(type.equals(String.class)) {
      return valueOf((String) object);
    }
    if(type.equals(Value.class)) {
      Value value = (Value) object;
      return value.isNull() ? nullValue() : valueOf(value.getValue());
    }
    return valueOf(object.toString());
  }

  @Override
  public int compare(Value o1, Value o2) {
    return ValueComparator.INSTANCE.compare(o1, o2);
  }

  /**
   * Returns a {@code Value} that holds today's date.
   *
   * @return a new {@code Value} initialized with today's date.
   */
  public Value now() {
    return valueOf(new Date());
  }
}

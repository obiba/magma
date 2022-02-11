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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.json.JSONObject;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.support.ValueComparator;

public class DateType extends AbstractValueType {

  private static final long serialVersionUID = -149385659514790222L;

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<DateType> instance;

  /**
   * Preferred format.
   */
  private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * These are used to support common date formats.
   */
  private final SimpleDateFormat[] dateFormats = new SimpleDateFormat[] { //
      ISO_8601, //
      new SimpleDateFormat("yyyy/MM/dd"), //
      new SimpleDateFormat("yyyy.MM.dd"), //
      new SimpleDateFormat("yyyy MM dd"), //
      new SimpleDateFormat("dd-MM-yyyy"), //
      new SimpleDateFormat("dd/MM/yyyy"), //
      new SimpleDateFormat("dd.MM.yyyy"), //
      new SimpleDateFormat("dd MM yyyy") };

  private String dateFormatPatterns = "";

  private DateType() {
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

  @NotNull
  public static DateType get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new DateType());
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
    return MagmaDate.class;
  }

  @NotNull
  @Override
  public String getName() {
    return "date";
  }

  @Override
  public boolean acceptsJavaClass(@NotNull Class<?> clazz) {
    // MAGMA-166: Although the API states that this method should return true for Date instances, it conflicts with
    // DateTimeType.
    // There is a loss of precision if we map Date instances to this ValueType, so it is safer to not accept these
    // types.
    return MagmaDate.class.isAssignableFrom(clazz);// || Date.class.isAssignableFrom(clazz) ||
    // java.sql.Date.class.isAssignableFrom(clazz) ||
    // java.sql.Timestamp.class.isAssignableFrom(clazz) ||
    // Calendar.class.isAssignableFrom(clazz);
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable String string) {
    if(string == null) {
      return nullValue();
    }

    for(SimpleDateFormat format : dateFormats) {
      try {
        return parseDate(format, string);
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
      return Factory.newValue(this, new MagmaDate(format.parse(string)));
    }
  }

  @NotNull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null || object.equals(JSONObject.NULL)) {
      return nullValue();
    }

    Class<?> type = object.getClass();
    if(type.equals(MagmaDate.class)) {
      return Factory.newValue(this, (Serializable) object);
    }
    if(type.equals(Date.class)) {
      return Factory.newValue(this, new MagmaDate((Date) object));
    }
    if(Date.class.isAssignableFrom(type)) {
      return Factory.newValue(this, new MagmaDate(new Date(((Date) object).getTime())));
    }
    if(Calendar.class.isAssignableFrom(type)) {
      return Factory.newValue(this, new MagmaDate((Calendar) object));
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
    return valueOf(new MagmaDate(new Date()));
  }

  @Override
  protected String toString(Object object) {
    if(object != null) {
      synchronized(ISO_8601) {
        return ISO_8601.format(((MagmaDate) object).asDate());
      }
    }
    return null;
  }

}

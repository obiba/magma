package org.obiba.magma.type;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;

public class DateTimeType extends AbstractValueType {

  private static final long serialVersionUID = -149385659514790222L;

  private static WeakReference<DateTimeType> instance;

  private final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  /** These are used to support other formats that Magma may have used in the past. */
  private final SimpleDateFormat[] otherFormats = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz") };

  private DateTimeType() {

  }

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

  @Override
  public String getName() {
    return "datetime";
  }

  @Override
  public boolean acceptsJavaClass(Class<?> clazz) {
    return Date.class.isAssignableFrom(clazz) || java.sql.Date.class.isAssignableFrom(clazz) || java.sql.Timestamp.class.isAssignableFrom(clazz) || Calendar.class.isAssignableFrom(clazz);
  }

  @Override
  public String toString(Object object) {
    // DateFormat is not thread safe
    synchronized(ISO_8601) {
      return ISO_8601.format((Date) object);
    }
  }

  @Override
  public Value valueOf(String string) {
    if(string == null) {
      return nullValue();
    }

    try {
      // DateFormat is not thread safe
      synchronized(ISO_8601) {
        return Factory.newValue(this, ISO_8601.parse(string));
      }
    } catch(ParseException e) {
      for(SimpleDateFormat sdf : otherFormats) {
        try {
          synchronized(otherFormats) {
            return Factory.newValue(this, sdf.parse(string));
          }
        } catch(ParseException e1) {
          // ignore and try next supported format
        }
      }
      throw new IllegalArgumentException("Cannot parse date from string value '" + string + "'");
    }
  }

  @Override
  public Value valueOf(Object object) {
    if(object == null) {
      return nullValue();
    }
    Class<?> type = object.getClass();
    if(type.equals(Date.class)) {
      return Factory.newValue(this, (Date) object);
    } else if(Date.class.isAssignableFrom(type)) {
      return Factory.newValue(this, new Date(((Date) object).getTime()));
    } else if(Calendar.class.isAssignableFrom(type)) {
      Calendar c = (Calendar) object;
      return Factory.newValue(this, c.getTime());
    }
    throw new IllegalArgumentException("Cannot construct " + getClass().getSimpleName() + " from type " + object.getClass() + ".");
  }
}

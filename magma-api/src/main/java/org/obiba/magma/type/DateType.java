package org.obiba.magma.type;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.MagmaDate;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;

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

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @Nonnull
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

  @Nonnull
  @Override
  public String getName() {
    return "date";
  }

  @Override
  public boolean acceptsJavaClass(@Nonnull Class<?> clazz) {
    // MAGMA-166: Although the API states that this method should return true for Date instances, it conflicts with
    // DateTimeType.
    // There is a loss of precision if we map Date instances to this ValueType, so it is safer to not accept these
    // types.
    return MagmaDate.class.isAssignableFrom(clazz);// || Date.class.isAssignableFrom(clazz) ||
    // java.sql.Date.class.isAssignableFrom(clazz) ||
    // java.sql.Timestamp.class.isAssignableFrom(clazz) ||
    // Calendar.class.isAssignableFrom(clazz);
  }

  @Nonnull
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

  @Nonnull
  @Override
  public Value valueOf(@Nullable Object object) {
    if(object == null) {
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
      return valueOf(((Value) object).getValue());
    }
    return valueOf(object.toString());
  }

  @Override
  public int compare(Value o1, Value o2) {
    if (o1.isNull() && o2.isNull()) return 0;
    if (o1.isNull()) return -1;
    if (o2.isNull()) return 1;
    return ((MagmaDate) o1.getValue()).compareTo((MagmaDate) o2.getValue());
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

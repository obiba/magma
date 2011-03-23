package org.obiba.magma;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * An immutable date without any time. Java does not have such a type and there is no way to re-use java.util.Date since
 * it is already used for representing date and time values.
 * @see org.obiba.magma.type.DateType
 */
public class MagmaDate implements Serializable, Comparable<MagmaDate> {

  private static final long serialVersionUID = 1L;

  private final int year;

  private final int month;

  private final int dayOfMonth;

  private transient int hashCode;

  private transient String toString;

  public MagmaDate(final Calendar calendar) {
    if(calendar == null) throw new IllegalArgumentException("calendar cannot be null");
    this.year = calendar.get(Calendar.YEAR);
    this.month = calendar.get(Calendar.MONTH);
    this.dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
  }

  public MagmaDate(int year, int month, int dayOfMonth) {
    this(asCalendar(year, month, dayOfMonth));
  }

  public MagmaDate(final Date date) {
    this(asCalendar(date));
  }

  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month;
  }

  public int getDayOfMonth() {
    return dayOfMonth;
  }

  public Calendar asCalendar() {
    Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Calendar.YEAR, year);
    c.set(Calendar.MONTH, month);
    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    return c;
  }

  public Date asDate() {
    return asCalendar().getTime();
  }

  @Override
  public int compareTo(MagmaDate o) {
    return 100 * (this.year - o.year)//
        - 10 * (this.month - o.month)//
        - (this.dayOfMonth - o.dayOfMonth);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null) return false;
    if(obj instanceof MagmaDate) {
      MagmaDate rhs = (MagmaDate) obj;
      return this.year == rhs.year && this.month == rhs.month && this.dayOfMonth == rhs.dayOfMonth;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // Lazily initialized, cached hashCode
    if(hashCode == 0) {
      int result = 17;
      result = 37 * result + this.year;
      result = 37 * result + this.month;
      result = 37 * result + this.dayOfMonth;
      hashCode = result;
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return toString != null ? toString : (toString = new StringBuilder().append(year).append('-').append(month + 1).append('-').append(dayOfMonth).toString());
  }

  private static Calendar asCalendar(final Date date) {
    if(date == null) throw new IllegalArgumentException("date cannot be null");
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }

  private static Calendar asCalendar(int year, int month, int dayOfMonth) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(year, month, dayOfMonth);
    return calendar;
  }
}

package org.obiba.magma.js.methods;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.IntegerType;

public class DateTimeMethods {

  /**
   * <pre>
   *   $('Date').year()
   * </pre>
   */
  public static Scriptable year(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.YEAR);
  }

  /**
   * Returns the month of a Date as an integer starting from 0 (January).
   * 
   * <pre>
   *   $('Date').month()
   * </pre>
   */
  public static Scriptable month(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.MONTH);
  }

  /**
   * Returns the day of week from a Date as an integer starting from 1 (Sunday).
   * 
   * <pre>
   *   $('Date').dayOfWeek()
   * </pre>
   */
  public static Scriptable dayOfWeek(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.DAY_OF_WEEK);
  }

  /**
   * Returns a boolean value indicating whether the date denotes a weekday (between Monday and Friday inclusively)
   * 
   * <pre>
   *   $('Date').weekday()
   * </pre>
   */
  public static Scriptable weekday(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Calendar c = asCalendar(thisObj);
    if(c != null) {
      int dow = c.get(Calendar.DAY_OF_WEEK);
      return new ScriptableValue(thisObj, BooleanType.get().valueOf(dow > Calendar.SUNDAY && dow < Calendar.SATURDAY));
    }
    return thisObj;
  }

  /**
   * Returns a boolean value indicating whether the date denotes a weekend (either Sunday or Saturday)
   * 
   * <pre>
   *   $('Date').weekend()
   * </pre>
   */
  public static Scriptable weekend(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Calendar c = asCalendar(thisObj);
    if(c != null) {
      int dow = c.get(Calendar.DAY_OF_WEEK);
      return new ScriptableValue(thisObj, BooleanType.get().valueOf(dow < Calendar.MONDAY && dow > Calendar.FRIDAY));
    }
    return thisObj;
  }

  /**
   * Returns the day of month from a Date as an integer starting from 1
   * 
   * <pre>
   *   $('Date').dayOfMonth()
   * </pre>
   */
  public static Scriptable dayOfMonth(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.DAY_OF_MONTH);
  }

  /**
   * Returns the day of year from a Date as an integer starting from 1
   * 
   * <pre>
   *   $('Date').dayOfYear()
   * </pre>
   */
  public static Scriptable dayOfYear(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.DAY_OF_YEAR);
  }

  /**
   * Returns the week of year from a Date as an integer starting from 1
   * 
   * <pre>
   *   $('Date').weekOfYear()
   * </pre>
   */
  public static Scriptable weekOfYear(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.WEEK_OF_YEAR);
  }

  /**
   * Returns the week of month from a Date as an integer starting from 1
   * 
   * <pre>
   *   $('Date').weekOfMonth()
   * </pre>
   */
  public static Scriptable weekOfMonth(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.WEEK_OF_MONTH);
  }

  /**
   * Converts a {@code ScriptableValue} instance to a {@code Calendar} instance. If {@code Value#isNull()} returns true,
   * this method returns null.
   * 
   * @param obj
   * @return
   */
  private static Calendar asCalendar(Scriptable obj) {
    ScriptableValue sv = (ScriptableValue) obj;
    Value value = sv.getValue();
    if(value.isNull() == false) {
      Date date = (Date) value.getValue();
      Calendar c = GregorianCalendar.getInstance();
      c.setTimeInMillis(date.getTime());
      return c;
    }
    return null;
  }

  /**
   * Given a {@code ScriptableValue}, this method extracts a {@code field} from the Calendar.
   * 
   * @param scope
   * @param obj
   * @param field
   * @return
   */
  private static Scriptable asScriptable(Scriptable scope, Scriptable value, int field) {
    Calendar c = asCalendar(value);
    if(c != null) {
      return asScriptable(scope, c.get(field));
    }
    return new ScriptableValue(scope, IntegerType.get().nullValue());
  }

  private static Scriptable asScriptable(Scriptable scope, int value) {
    return new ScriptableValue(scope, IntegerType.get().valueOf(value));
  }

}

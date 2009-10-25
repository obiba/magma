package org.obiba.meta.js;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

import com.google.common.collect.ImmutableSet;

public final class DateTimeMethods {

  public static Set<String> exposedMethods = new ImmutableSet.Builder<String>().add("now", "dateYear", "dateMonth", "dateDayOfWeek", "dateDayOfMonth", "dateDayOfYear").build();

  public static Scriptable now(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return Context.toObject(ScriptRuntime.wrapNumber(new Date().getTime()), thisObj);
  }

  public static int dateYear(Scriptable scriptable) {
    return scriptable != null ? asCalendar(scriptable).get(Calendar.YEAR) : null;
  }

  public static int dateMonth(Scriptable scriptable) {
    return scriptable != null ? asCalendar(scriptable).get(Calendar.MONTH) : null;
  }

  public static int dateDayOfWeek(Scriptable scriptable) {
    return scriptable != null ? asCalendar(scriptable).get(Calendar.DAY_OF_WEEK) : null;
  }

  public static int dateDayOfMonth(Scriptable scriptable) {
    return scriptable != null ? asCalendar(scriptable).get(Calendar.DAY_OF_MONTH) : null;
  }

  public static int dateDayOfYear(Scriptable scriptable) {
    return scriptable != null ? asCalendar(scriptable).get(Calendar.DAY_OF_YEAR) : null;
  }

  private static Calendar asCalendar(Scriptable scriptable) {
    long l = (long) ScriptRuntime.toNumber(scriptable);
    Calendar c = GregorianCalendar.getInstance();
    c.setTimeInMillis(l);
    return c;
  }

}

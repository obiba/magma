package org.obiba.magma.js.methods;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.annotation.Nullable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

/**
 * Methods of the {@code ScriptableValue} javascript class that deal with {@code ScriptableValue} of {@code DateType}.
 */
@SuppressWarnings("UnusedDeclaration")
public class DateTimeMethods {

  private DateTimeMethods() {
  }

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
   * <p/>
   * <pre>
   *   $('Date').month()
   * </pre>
   */
  public static Scriptable month(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.MONTH);
  }

  /**
   * Returns the quarter of a Date as an integer starting from 0 (January-March)
   * <p/>
   * <pre>
   *   $('Date').quarter()
   * </pre>
   */
  public static Scriptable quarter(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Value currentValue = ((ScriptableValue) thisObj).getValue();
    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, IntegerType.get().nullSequence());
      }
      Collection<Value> newValues = new ArrayList<Value>();
      //noinspection ConstantConditions
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(quarter(value));
      }
      return new ScriptableValue(thisObj, IntegerType.get().sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj, quarter(currentValue));
    }
  }

  private static Value quarter(Value value) {
    Calendar c = asCalendar(value);
    if(c != null) {
      int month = c.get(Calendar.MONTH);
      int quarter = 3;
      if(month < 3) {
        quarter = 0;
      } else if(month < 6) {
        quarter = 1;
      } else if(month < 9) {
        quarter = 2;
      }
      return IntegerType.get().valueOf(quarter);
    }
    return IntegerType.get().nullValue();
  }

  /**
   * Returns the semester of a Date as an integer starting from 0 (January-June)
   * <p/>
   * <pre>
   *   $('Date').semester()
   * </pre>
   */
  public static Scriptable semester(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Value currentValue = ((ScriptableValue) thisObj).getValue();
    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, IntegerType.get().nullSequence());
      }
      Collection<Value> newValues = new ArrayList<Value>();
      //noinspection ConstantConditions
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(semester(value));
      }
      return new ScriptableValue(thisObj, IntegerType.get().sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj, semester(currentValue));
    }
  }

  private static Value semester(Value value) {
    Calendar c = asCalendar(value);
    if(c != null) {
      int month = c.get(Calendar.MONTH);
      int semester = 1;
      if(month < 6) {
        semester = 0;
      }
      return IntegerType.get().valueOf(semester);
    }
    return IntegerType.get().nullValue();
  }

  /**
   * Returns the day of week from a Date as an integer starting from 1 (Sunday).
   * <p/>
   * <pre>
   *   $('Date').dayOfWeek()
   * </pre>
   */
  public static Scriptable dayOfWeek(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.DAY_OF_WEEK);
  }

  /**
   * Returns a boolean value indicating whether the date denotes a weekday (between Monday and Friday inclusively)
   * <p/>
   * <pre>
   *   $('Date').weekday()
   * </pre>
   */
  public static Scriptable weekday(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Value currentValue = ((ScriptableValue) thisObj).getValue();
    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, BooleanType.get().nullSequence());
      }
      Collection<Value> newValues = new ArrayList<Value>();
      //noinspection ConstantConditions
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(weekday(value));
      }
      return new ScriptableValue(thisObj, BooleanType.get().sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj, weekday(currentValue));
    }
  }

  private static Value weekday(Value value) {
    Calendar c = asCalendar(value);
    if(c != null) {
      int dow = c.get(Calendar.DAY_OF_WEEK);
      return BooleanType.get().valueOf(dow > Calendar.SUNDAY && dow < Calendar.SATURDAY);
    }
    return BooleanType.get().nullValue();
  }

  /**
   * Returns a boolean value indicating whether the date denotes a weekend (either Sunday or Saturday)
   * <p/>
   * <pre>
   *   $('Date').weekend()
   * </pre>
   */
  public static Scriptable weekend(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Value currentValue = ((ScriptableValue) thisObj).getValue();
    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, BooleanType.get().nullSequence());
      }
      Collection<Value> newValues = new ArrayList<Value>();
      //noinspection ConstantConditions
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(weekend(value));
      }
      return new ScriptableValue(thisObj, BooleanType.get().sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj, weekend(currentValue));
    }
  }

  private static Value weekend(Value value) {
    Calendar c = asCalendar(value);
    if(c != null) {
      int dow = c.get(Calendar.DAY_OF_WEEK);
      return BooleanType.get().valueOf(dow < Calendar.MONDAY || dow > Calendar.FRIDAY);
    }
    return BooleanType.get().nullValue();
  }

  /**
   * Returns the day of month from a Date as an integer starting from 1
   * <p/>
   * <pre>
   *   $('Date').dayOfMonth()
   * </pre>
   */
  public static Scriptable dayOfMonth(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.DAY_OF_MONTH);
  }

  /**
   * Returns the day of year from a Date as an integer starting from 1
   * <p/>
   * <pre>
   *   $('Date').dayOfYear()
   * </pre>
   */
  public static Scriptable dayOfYear(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.DAY_OF_YEAR);
  }

  /**
   * Returns the week of year from a Date as an integer starting from 1
   * <p/>
   * <pre>
   *   $('Date').weekOfYear()
   * </pre>
   */
  public static Scriptable weekOfYear(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.WEEK_OF_YEAR);
  }

  /**
   * Returns the week of month from a Date as an integer starting from 1
   * <p/>
   * <pre>
   *   $('Date').weekOfMonth()
   * </pre>
   */
  public static Scriptable weekOfMonth(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.WEEK_OF_MONTH);
  }

  /**
   * Returns the hour of the day for the 24-hour clock. For example, at 10:04:15.250 PM the hour of the day is 22.
   * <p/>
   * <pre>
   *   $('Date').hourOfDay()
   * </pre>
   */
  public static Scriptable hourOfDay(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.HOUR_OF_DAY);
  }

  /**
   * Returns the hour of the day for the 12-hour clock (0 - 11). Noon and midnight are represented by 0, not by 12. For
   * example, at 10:04:15.250 PM the HOUR is 10.
   * <p/>
   * <pre>
   *   $('Date').hour()
   * </pre>
   */
  public static Scriptable hour(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.HOUR);
  }

  /**
   * Returns the minute within the hour.
   * <p/>
   * <pre>
   *   $('Date').minute()
   * </pre>
   */
  public static Scriptable minute(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.MINUTE);
  }

  /**
   * Returns the second within the minute.
   * <p/>
   * <pre>
   *   $('Date').second()
   * </pre>
   */
  public static Scriptable second(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.SECOND);
  }

  /**
   * Returns the millisecond within the second.
   * <p/>
   * <pre>
   *   $('Date').millisecond()
   * </pre>
   */
  public static Scriptable millisecond(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    return asScriptable(thisObj, thisObj, Calendar.MILLISECOND);
  }

  /**
   * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT (epoch time).
   * <p/>
   * <pre>
   *   $('Date').time()
   * </pre>
   */
  public static Scriptable time(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Value currentValue = ((ScriptableValue) thisObj).getValue();

    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, IntegerType.get().nullSequence());
      }
      Collection<Value> newValues = new ArrayList<Value>();
      //noinspection ConstantConditions
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(timeValue(value));
      }
      return new ScriptableValue(thisObj, TextType.get().sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj, timeValue(currentValue));
    }
  }

  private static Value timeValue(Value value) {
    if(value.isNull()) return IntegerType.get().nullValue();
    //noinspection ConstantConditions
    return IntegerType.get().valueOf(asDate(value).getTime());
  }

  /**
   * Returns the text representation of the date formatted as specified by the provided pattern.
   * <p/>
   * <pre>
   *   $('Date').format('dd/MM/yyyy')
   * </pre>
   *
   * @see java.text.SimpleDateFormat
   */
  public static Scriptable format(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    Value currentValue = ((ScriptableValue) thisObj).getValue();
    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, TextType.get().nullSequence());
      }
      Collection<Value> newValues = new ArrayList<Value>();
      //noinspection ConstantConditions
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(format(value, args));
      }
      return new ScriptableValue(thisObj, TextType.get().sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj, format(currentValue, args));
    }
  }

  @SuppressWarnings({ "ChainOfInstanceofChecks", "PMD.NcssMethodCount" })
  private static Value format(Value value, Object... args) {
    if(args == null || args.length == 0) {
      return TextType.get().nullValue();
    }

    Date date = asDate(value);
    if(date == null) {
      return TextType.get().nullValue();
    }

    SimpleDateFormat format = null;
    Object arg = args[0];
    if(arg instanceof ScriptableValue) {
      ScriptableValue operand = (ScriptableValue) arg;
      if(operand.getValue().isSequence()) {
        throw new MagmaJsEvaluationRuntimeException("Argument to format() method must not be a sequence of values.");
      }
      if(operand.getValue().isNull()) {
        return TextType.get().nullValue();
      }
      format = new SimpleDateFormat(arg.toString());
    } else if(arg instanceof String) {
      format = new SimpleDateFormat((String) arg);
    } else {
      throw new MagmaJsEvaluationRuntimeException("Argument to format() method must be a String or a ScriptableValue.");
    }

    return TextType.get().valueOf(format.format(date));
  }

  /**
   * Returns true if this Date value is after the specified date value(s)
   * <p/>
   * <pre>
   *   $('Date').after($('OtherDate'))
   *   $('Date').after($('OtherDate'), $('SomeOtherDate'))
   * </pre>
   */
  public static Scriptable after(Context cx, Scriptable thisObj, Object[] args, @Nullable Function funObj) {
    if(args == null || args.length == 0) {
      return new ScriptableValue(thisObj, BooleanType.get().falseValue());
    }

    Value currentValue = ((ScriptableValue) thisObj).getValue();
    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, BooleanType.get().nullSequence());
      }
      Collection<Value> newValues = new ArrayList<Value>();
      //noinspection ConstantConditions
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(after(value, args));
      }
      return new ScriptableValue(thisObj, BooleanType.get().sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj, after(currentValue, args));
    }
  }

  private static Value after(Value value, Object... args) {
    Calendar thisCalendar = asCalendar(value);
    if(thisCalendar == null) {
      return BooleanType.get().nullValue();
    }

    for(Object arg : args) {
      if(arg instanceof ScriptableValue) {
        ScriptableValue operand = (ScriptableValue) arg;
        if(operand.getValue().isSequence()) {
          throw new MagmaJsEvaluationRuntimeException("Operand to after() method must not be a sequence of values.");
        }
        Calendar c = asCalendar(operand.getValue());
        if(c == null) {
          return BooleanType.get().nullValue();
        }
        if(thisCalendar.before(c)) {
          return BooleanType.get().falseValue();
        }
      } else {
        throw new MagmaJsEvaluationRuntimeException("Operand to after() method must be a ScriptableValue.");
      }
    }
    return BooleanType.get().trueValue();
  }

  @Nullable
  private static Date asDate(Value value) {

    if(value.getValueType() == DateTimeType.get()) {
      if(!value.isNull()) {
        return (Date) value.getValue();
      }
    } else if(value.getValueType() == DateType.get()) {
      if(!value.isNull()) {
        //noinspection ConstantConditions
        return ((MagmaDate) value.getValue()).asDate();
      }
    } else {
      throw new MagmaJsEvaluationRuntimeException(
          "Invalid ValueType: expected '" + DateTimeType.get().getName() + "' or '" + DateType.get().getName() +
              "' got '" + value.getValueType().getName() + "'");
    }
    return null;
  }

  /**
   * Converts a {@code Value} instance to a {@code Calendar} instance. If {@code Value#isNull()} returns true, this
   * method returns null.
   *
   * @param value
   * @return
   */
  @SuppressWarnings("ConstantConditions")
  @Nullable
  private static Calendar asCalendar(Value value) {
    if(value.getValueType() == DateTimeType.get()) {
      if(!value.isNull()) {
        Date date = (Date) value.getValue();
        Calendar c = GregorianCalendar.getInstance();
        c.setTimeInMillis(date.getTime());
        return c;
      }
    } else if(value.getValueType() == DateType.get()) {
      if(!value.isNull()) {
        return ((MagmaDate) value.getValue()).asCalendar();
      }
    } else {
      throw new MagmaJsEvaluationRuntimeException(
          "Invalid ValueType: expected '" + DateTimeType.get().getName() + "' or '" + DateType.get().getName() +
              "' got '" + value.getValueType().getName() + "'");
    }
    return null;
  }

  /**
   * Given a {@code ScriptableValue}, this method extracts a {@code field} from the Calendar.
   *
   * @param scope
   * @param sv
   * @param field
   * @return
   */
  @SuppressWarnings("MagicConstant")
  private static Scriptable asScriptable(Scriptable scope, Scriptable sv, int field) {
    Value currentValue = ((ScriptableValue) sv).getValue();

    if(currentValue.isSequence()) {
      return asScriptable(scope, currentValue.asSequence(), field);
    } else {
      Calendar c = asCalendar(currentValue);
      if(c != null) {
        return asScriptable(scope, c.get(field));
      }
      return new ScriptableValue(scope, IntegerType.get().nullValue());
    }
  }

  @SuppressWarnings("MagicConstant")
  private static Scriptable asScriptable(Scriptable scope, ValueSequence currentValue, int field) {
    if(currentValue.isNull()) {
      return new ScriptableValue(scope, IntegerType.get().nullSequence());
    }
    Collection<Value> newValues = new ArrayList<Value>();
    //noinspection ConstantConditions
    for(Value value : currentValue.asSequence().getValue()) {
      Calendar c = asCalendar(value);
      if(c != null) {
        newValues.add(IntegerType.get().valueOf(c.get(field)));
      } else {
        newValues.add(IntegerType.get().nullValue());
      }
    }
    return new ScriptableValue(scope, IntegerType.get().sequenceOf(newValues));
  }

  private static Scriptable asScriptable(Scriptable scope, int value) {
    return new ScriptableValue(scope, IntegerType.get().valueOf(value));
  }

  /**
   * Adds days to a {@code ScriptableValue} of {@code DateType}.
   * <p/>
   * <pre>
   *   $('Date').add(2)  // Adds 2 days.
   *   $('Date').add(-4) // Subtracts 4 days.
   * </pre>
   */
  public static Scriptable add(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new UnsupportedOperationException(".add() expects exactly one integer argument: days to add.");
    }

    Value currentValue = ((ScriptableValue) thisObj).getValue();
    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, DateTimeType.get().nullSequence());
      }
      Collection<Value> newValues = new ArrayList<Value>();
      //noinspection ConstantConditions
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(add(value, args));
      }
      return new ScriptableValue(thisObj, DateTimeType.get().sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj, add(currentValue, args));
    }
  }

  private static Value add(Value cvalue, Object... args) {
    Calendar c = asCalendar(cvalue);
    if(c != null) {
      int argument = 0;
      if(args[0] instanceof ScriptableValue) {
        Value value = ((ScriptableValue) args[0]).getValue();
        argument = Integer.parseInt(value.getValue().toString());
      } else {
        argument = ((Number) args[0]).intValue();
      }
      c.add(Calendar.DAY_OF_MONTH, argument);
      return DateTimeType.get().valueOf(c);
    }
    return cvalue;
  }
}

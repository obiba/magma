/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.js.methods;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.RegExpProxy;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.Rhino;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Methods of the {@code ScriptableValue} javascript class that returns {@code ScriptableValue} of {@code BooleanType}
 */
@SuppressWarnings({ "UnusedParameters", "IfMayBeConditional", "UnusedDeclaration", "StaticMethodOnlyUsedInOneClass" })
public class TextMethods {

  private TextMethods() {
  }

  /**
   * <pre>
   *   $('TextVar').trim()
   * </pre>
   */
  public static ScriptableValue trim(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    ValueFunction trimFunction = new ValueFunction() {

      @SuppressWarnings("ConstantConditions")
      @Override
      public Value apply(Value input) {
        if(input == null || input.isNull()) return TextType.get().nullValue();
        return TextType.get().valueOf(input.toString().trim());
      }

      @Override
      public ValueType getValueType() {
        return TextType.get();
      }
    };

    return transformValue((ScriptableValue) thisObj, trimFunction);
  }

  /**
   * <pre>
   *   $('TextVar').upperCase()
   *   $('TextVar').upperCase('fr')
   * </pre>
   */
  public static ScriptableValue upperCase(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    final Locale locale = getLocaleArgument(args);
    ValueFunction caseFunction = new ValueFunction() {

      @Override
      @SuppressWarnings("ConstantConditions")
      public Value apply(Value input) {
        if(input == null || input.isNull()) return TextType.get().nullValue();
        String stringValue = input.toString();
        stringValue = locale == null ? stringValue.toUpperCase() : stringValue.toUpperCase(locale);
        return TextType.get().valueOf(stringValue);
      }

      @Override
      public ValueType getValueType() {
        return TextType.get();
      }
    };

    return transformValue((ScriptableValue) thisObj, caseFunction);
  }

  /**
   * <pre>
   *   $('TextVar').lowerCase()
   *   $('TextVar').lowerCase('fr')
   * </pre>
   */
  public static ScriptableValue lowerCase(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    final Locale locale = getLocaleArgument(args);
    ValueFunction caseFunction = new ValueFunction() {

      @Override
      @SuppressWarnings("ConstantConditions")
      public Value apply(Value input) {
        if(input == null || input.isNull()) return TextType.get().nullValue();
        String stringValue = input.toString();
        stringValue = locale == null ? stringValue.toLowerCase() : stringValue.toLowerCase(locale);
        return TextType.get().valueOf(stringValue);
      }

      @Override
      public ValueType getValueType() {
        return TextType.get();
      }
    };

    return transformValue((ScriptableValue) thisObj, caseFunction);
  }

  @Nullable
  private static Locale getLocaleArgument(@Nullable Object... args) {
    if(args != null && args.length > 0) {
      Object localeArg = args[0];
      Value localeValue = localeArg instanceof ScriptableValue
          ? ((ScriptableValue) localeArg).getValue()
          : LocaleType.get().valueOf(localeArg);
      return localeValue.isNull() ? null : (Locale) localeValue.getValue();
    }
    return null;
  }

  /**
   * <pre>
   *   $('TextVar').capitalize()
   *   $('TextVar').capitalize(':;_.,(')
   * </pre>
   */
  public static ScriptableValue capitalize(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    return transformValue((ScriptableValue) thisObj, new CapitalizeFunction(getDelim(args)));
  }

  private static class CapitalizeFunction implements ValueFunction {

    private final String delim;

    private CapitalizeFunction(String delim) {
      this.delim = delim;
    }

    @Override
    public Value apply(Value input) {
      if(input == null || input.isNull()) {
        return TextType.get().nullValue();
      }
      @SuppressWarnings("ConstantConditions")
      char[] buffer = input.toString().toCharArray();
      boolean capitalizeNext = true;
      for(int i = 0; i < buffer.length; i++) {
        char ch = buffer[i];
        if(isDelimiter(ch, delim)) {
          capitalizeNext = true;
        } else if(capitalizeNext) {
          buffer[i] = Character.toTitleCase(ch);
          capitalizeNext = false;
        }
      }
      return TextType.get().valueOf(new String(buffer));
    }

    @Override
    public ValueType getValueType() {
      return TextType.get();
    }

    private boolean isDelimiter(char ch, @Nullable String delimiters) {
      if(delimiters == null || delimiters.isEmpty()) {
        return Character.isWhitespace(ch);
      }
      for(char delimiter : delimiters.toCharArray()) {
        if(ch == delimiter) {
          return true;
        }
      }
      return false;
    }
  }

  @Nullable
  private static String getDelim(@Nullable Object... args) {
    if(args == null) return null;
    StringBuilder sb = new StringBuilder();
    for(Object arg : args) {
      if(arg != null) sb.append(arg.toString());
    }
    return sb.toString();
  }

  /**
   * <pre>
   *   $('TextVar').replace('regex', '$1')
   * </pre>
   */
  public static ScriptableValue replace(final Context ctx, final Scriptable thisObj, final Object[] args,
      @Nullable Function funObj) {
    ValueFunction replaceFunction = new ValueFunction() {

      @Override
      public Value apply(Value input) {
        String stringValue = input == null || input.isNull() ? null : input.toString();

        // Delegate to Javascript's String.replace method
        String result = (String) ScriptRuntime.checkRegExpProxy(ctx)
            .action(ctx, thisObj, ScriptRuntime.toObject(ctx, thisObj, stringValue), args, RegExpProxy.RA_REPLACE);

        return TextType.get().valueOf(result);
      }

      @Override
      public ValueType getValueType() {
        return TextType.get();
      }
    };
    return transformValue((ScriptableValue) thisObj, replaceFunction);
  }

  /**
   * <pre>
   *   $('TextVar').matches('regex1', 'regex2', ...)
   * </pre>
   */
  public static ScriptableValue matches(final Context ctx, final Scriptable thisObj, final Object[] args,
      @Nullable Function funObj) {
    ValueFunction matchesFunction = new ValueFunction() {

      @Override
      public Value apply(Value input) {
        String stringValue = input == null || input.isNull() ? null : input.toString();

        // Delegate to Javascript's String.replace method
        boolean matches = false;
        if(stringValue != null) {
          for(Object arg : args) {
            Object result = ScriptRuntime.checkRegExpProxy(ctx)
                .action(ctx, thisObj, ScriptRuntime.toObject(ctx, thisObj, stringValue), new Object[] { arg },
                    RegExpProxy.RA_MATCH);
            if(result != null) {
              matches = true;
            }
          }
        }

        return BooleanType.get().valueOf(matches);
      }

      @Override
      public ValueType getValueType() {
        return BooleanType.get();
      }
    };
    return transformValue((ScriptableValue) thisObj, matchesFunction);
  }

  /**
   * Returns a new {@link ScriptableValue} of {@link TextType} combining the String value of this value with the String
   * values of the parameters parameters.
   * <p/>
   * <pre>
   *   $('TextVar').concat($('TextVar'))
   *   $('Var').concat($('Var'))
   *   $('Var').concat('SomeValue')
   * </pre>
   */
  public static ScriptableValue concat(Context ctx, Scriptable thisObj, final Object[] args,
      @Nullable Function funObj) {
    ValueFunction concatFunction = new ValueFunction() {

      @Override
      public Value apply(Value input) {
        String stringValue = input == null || input.isNull() ? null : input.toString();
        StringBuilder sb = new StringBuilder();
        sb.append(stringValue);
        if(args != null) {
          for(Object arg : args) {
            if(arg instanceof ScriptableValue) {
              arg = arg.toString();
            }
            sb.append(arg);
          }
        }
        return TextType.get().valueOf(sb.toString());
      }

      @Override
      public ValueType getValueType() {
        return TextType.get();
      }
    };
    return transformValue((ScriptableValue) thisObj, concatFunction);
  }

  /**
   * Categorise values of a variable. That is, lookup the current value in an association table and return the
   * associated value. When the current value is not found in the association table, the method returns a null value.
   * <p/>
   * <pre>
   * $('SMOKE').map({'NO':0, 'YES':1, 'DNK':8888, 'PNA':9999})
   *
   * $('SMOKE_ONSET').map(
   *   {'AGE':$('SMOKE_ONSET_AGE'),
   *    'YEAR':$('SMOKE_ONSET_YEAR').minus($('BIRTH_DATE').year()),
   *    'DNK':8888,
   *    'PNA':9999})
   *
   * // Works for sequences also (FRENCH,ENGLISH --&gt; 0,1)
   * $('LANGUAGES_SPOKEN').map({'FRENCH':0, 'ENGLISH':1});
   *
   * // Specification of default value
   * $('LANGUAGES_SPOKEN').map({'FRENCH':0, 'ENGLISH':1}, 99);
   *
   * // Specification of default value and null value mapping
   * $('LANGUAGES_SPOKEN').map({'FRENCH':0, 'ENGLISH':1}, 99, 88);
   *
   * // Can execute function to calculate lookup value
   * $('BMI_DIAG').map(
   *   {'OVERW': function(value) {
   *               // 'OVERW' is passed in as the method's parameter
   *               var computedValue = 2*2; // some complex computation...
   *               return comuptedValue;
   *             },
   *    'NORMW': 0
   *   }
   *
   * // Can execute function to calculate default value
   * $('LANGUAGES_SPOKEN').map({'FRENCH':0, 'ENGLISH':1}, function(v){ return 99; });
   *
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   */
  public static ScriptableValue map(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args == null || args.length < 1 || !(args[0] instanceof NativeObject)) {
      throw new MagmaJsEvaluationRuntimeException("illegal arguments to map()");
    }

    ScriptableValue sv = (ScriptableValue) thisObj;
    Scriptable valueMap = (Scriptable) args[0];

    // This could be determined by looking at the mapped values (if all ints, then 'integer', else 'text', etc.)
    ValueType returnType = TextType.get();

    Object defaultArg = args.length < 2 ? null : args[1];
    Object nullArg = args.length < 3 ? null : args[2];

    Value currentValue = sv.getValue();
    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, returnType.nullSequence());
      }
      Collection<Value> newValues = new ArrayList<>();
      //noinspection ConstantConditions
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(lookupValue(ctx, thisObj, value, returnType, valueMap, defaultArg, nullArg));
      }
      return new ScriptableValue(thisObj, returnType.sequenceOf(newValues));
    }
    return new ScriptableValue(thisObj,
        lookupValue(ctx, thisObj, currentValue, returnType, valueMap, defaultArg, nullArg));
  }

  /**
   * Tries to convert value of any type to a Date value given a date format.
   * <pre>
   *   $('VAR').date('MM/dd/yy')
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   */
  public static ScriptableValue date(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;

    // Return the ValueType name
    if(args.length == 0 || args[0] == null) {
      throw new MagmaJsEvaluationRuntimeException("date format is missing to date()");
    }
    return getDateValue(thisObj, DateType.get(), sv.getValue(), args[0].toString());
  }

  /**
   * Tries to convert value of any type to a DateTime value given a date format.
   * <pre>
   *   $('VAR').date('MM/dd/yy HH:mm')
   * </pre>
   *
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   */
  public static ScriptableValue datetime(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;

    // Return the ValueType name
    if(args.length == 0 || args[0] == null) {
      throw new MagmaJsEvaluationRuntimeException("date time format is missing to datetime()");
    }
    return getDateValue(thisObj, DateTimeType.get(), sv.getValue(), args[0].toString());
  }

  private static ScriptableValue getDateValue(Scriptable thisObj, final ValueType dateType, Value value,
      String formatArg) {
    final SimpleDateFormat format = new SimpleDateFormat(formatArg);
    ValueFunction dateFunction = new ValueFunction() {

      @Override
      public Value apply(Value input) {
        if(input == null || input.isNull()) return dateType.nullValue();

        String inputStr = input.toString();

        if(inputStr == null || inputStr.trim().isEmpty()) return dateType.nullValue();

        try {
          Date date = format.parse(inputStr);
          return dateType.valueOf(date);
        } catch(ParseException e) {
          throw new MagmaJsEvaluationRuntimeException(
              "date/datetime format '" + format.toPattern() + "' fails to parse: '" + inputStr + "'");
        }
      }

      @Override
      public ValueType getValueType() {
        return dateType;
      }
    };
    return transformValue((ScriptableValue) thisObj, dateFunction);
  }

  /**
   * Returns the default value to use when the lookup value is not found in the map. This method is used by the map()
   * method.
   *
   * @param returnType
   * @param defaultArg
   * @param value Currently evaluated value
   * @return
   */
  private static Value defaultValue(Context ctx, Scriptable thisObj, ValueType returnType, Object defaultArg, Value value) {
    if(defaultArg == null) {
      // No default value was specified. Return null.
      return returnType.nullValue();
    }

    if(defaultArg instanceof ScriptableValue) {
      return ((ScriptableValue) defaultArg).getValue();
    }
    Object newValue = defaultArg;
    if(defaultArg instanceof Function) {
      Callable valueFunction = (Callable) defaultArg;
      Object evaluatedValue = valueFunction.call(ctx, thisObj, thisObj,
          value == null ? new Object[] {} : new Object[] { new ScriptableValue(thisObj, value) });
      newValue = evaluatedValue instanceof ScriptableValue
          ? ((ScriptableValue) evaluatedValue).getValue().getValue()
          : evaluatedValue;
    }
    return returnType.valueOf(Rhino.fixRhinoNumber(newValue));
  }

  /**
   * Returns the value to use when the lookup value is null. This method is used by the map() method.
   *
   * @param returnType
   * @param nullArg
   * @return
   */
  private static Value nullValue(Context ctx, Scriptable thisObj, ValueType returnType, Object defaultArg, Object nullArg) {
    if(nullArg == null) {
      // No value for null was specified. Return what is defined as default value.
      return defaultValue(ctx, thisObj, returnType, defaultArg, null);
    }

    if(nullArg instanceof ScriptableValue) {
      return ((ScriptableValue) nullArg).getValue();
    }
    Object newValue = nullArg;
    if(nullArg instanceof Function) {
      Callable valueFunction = (Callable) nullArg;
      Object evaluatedValue = valueFunction.call(ctx, thisObj, thisObj, new Object[] {});
      newValue = evaluatedValue instanceof ScriptableValue
          ? ((ScriptableValue) evaluatedValue).getValue().getValue()
          : evaluatedValue;
    }
    return returnType.valueOf(Rhino.fixRhinoNumber(newValue));
  }

  /**
   * Lookup {@code value} in {@code valueMap} and return the mapped value of type {@code returnType}
   *
   * @param ctx
   * @param thisObj
   * @param value
   * @param returnType
   * @param valueMap
   * @return
   */
  @SuppressWarnings("PMD.ExcessiveParameterList")
  private static Value lookupValue(Context ctx, Scriptable thisObj, Value value, ValueType returnType,
      Scriptable valueMap, Object defaultArg, Object nullArg) {

    if(value.isNull()) return nullValue(ctx, thisObj, returnType, defaultArg, nullArg);

    // MAGMA-163: lookup using string and index-based keys
    String asName = value.toString();
    Object newValue = valueMap.get(asName, null);
    if(newValue == NativeObject.NOT_FOUND) {
      // Not found, try converting the input to an Integer and use an indexed-lookup if it works
      Integer index = asJsIndex(value);
      if(index != null) {
        newValue = valueMap.get(index, null);
      }
    }

    if(newValue == null) return returnType.nullValue();

    if(newValue == NativeObject.NOT_FOUND) return defaultValue(ctx, thisObj, returnType, defaultArg, value);

    if(newValue instanceof Function) {
      Callable valueFunction = (Callable) newValue;
      Object evaluatedValue = valueFunction
          .call(ctx, thisObj, thisObj, new Object[] { new ScriptableValue(thisObj, value) });
      newValue = evaluatedValue instanceof ScriptableValue
          ? ((ScriptableValue) evaluatedValue).getValue().getValue()
          : evaluatedValue;
    }

    return returnType.valueOf(Rhino.fixRhinoNumber(newValue));
  }

  /**
   * Try to convert the input value as a index usable as a integer-based lookup
   *
   * @param value
   * @return
   */
  @Nullable
  private static Integer asJsIndex(Value value) {
    if(value.isNull()) return null;

    Number asNumber = null;
    if(value.getValueType() == IntegerType.get()) {
      asNumber = (Number) value.getValue();
    } else {
      try {
        // Try a conversion. Throws a runtime exception when it fails
        asNumber = (Number) IntegerType.get().convert(value).getValue();
      } catch(RuntimeException e) {
        // ignored
      }
    }
    if(asNumber != null) {
      return asNumber.intValue();
    }
    return null;
  }

  /**
   * Transform a value or values from a value sequence using the provided function.
   *
   * @param sv
   * @param valueFunction
   * @return
   */
  private static ScriptableValue transformValue(ScriptableValue sv,
      @NotNull ValueFunction valueFunction) {

    Value value = sv.getValue();
    if(value.isNull()) {
      return value.isSequence()
          ? new ScriptableValue(sv, valueFunction.getValueType().nullSequence())
          : new ScriptableValue(sv, valueFunction.apply(value));
    }

    if(value.isSequence()) {
      return new ScriptableValue(sv, valueFunction.getValueType()
          .sequenceOf(Lists.newArrayList(Iterables.transform(value.asSequence().getValue(), valueFunction))));
    }
    return new ScriptableValue(sv, valueFunction.apply(value));
  }

}

package org.obiba.magma.js.methods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    com.google.common.base.Function<Value, Value> trimFunction = new com.google.common.base.Function<Value, Value>() {

      @SuppressWarnings("ConstantConditions")
      @Override
      public Value apply(Value input) {
        if(input == null || input.isNull()) return TextType.get().nullValue();
        return TextType.get().valueOf(input.toString().trim());
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
    com.google.common.base.Function<Value, Value> caseFunction = new com.google.common.base.Function<Value, Value>() {

      @Override
      @SuppressWarnings("ConstantConditions")
      public Value apply(Value input) {
        if(input == null || input.isNull()) return TextType.get().nullValue();
        String stringValue = input.toString();
        stringValue = locale == null ? stringValue.toUpperCase() : stringValue.toUpperCase(locale);
        return TextType.get().valueOf(stringValue);
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
    com.google.common.base.Function<Value, Value> caseFunction = new com.google.common.base.Function<Value, Value>() {

      @Override
      @SuppressWarnings("ConstantConditions")
      public Value apply(Value input) {
        if(input == null || input.isNull()) return TextType.get().nullValue();
        String stringValue = input.toString();
        stringValue = locale == null ? stringValue.toLowerCase() : stringValue.toLowerCase(locale);
        return TextType.get().valueOf(stringValue);
      }
    };

    return transformValue((ScriptableValue) thisObj, caseFunction);
  }

  @Nullable
  private static Locale getLocaleArgument(@Nullable Object... args) {
    Locale locale = null;
    if(args != null && args.length > 0) {
      Object localeArg = args[0];
      Value localeValue = localeArg instanceof ScriptableValue
          ? ((ScriptableValue) localeArg).getValue()
          : LocaleType.get().valueOf(localeArg);
      locale = (Locale) localeValue.getValue();
    }
    return locale;
  }

  /**
   * <pre>
   *   $('TextVar').capitalize()
   *   $('TextVar').capitalize(':;_.,(')
   * </pre>
   */
  public static ScriptableValue capitalize(Context ctx, Scriptable thisObj, @Nullable Object[] args,
      @Nullable Function funObj) {
    final String delim;
    if(args != null) {
      StringBuilder buffer = new StringBuilder();
      for(Object arg : args) {
        if(arg != null) {
          buffer.append(arg.toString());
        }
      }
      delim = buffer.toString();
    } else {
      delim = null;
    }

    com.google.common.base.Function<Value, Value> capitalizeFunction
        = new com.google.common.base.Function<Value, Value>() {

      @SuppressWarnings("ConstantConditions")
      @Override
      public Value apply(Value input) {
        if(input == null || input.isNull()) return TextType.get().nullValue();
        String stringValue = input.toString();

        char[] buffer = stringValue.toCharArray();
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
    };

    return transformValue((ScriptableValue) thisObj, capitalizeFunction);
  }

  /**
   * <pre>
   *   $('TextVar').replace('regex', '$1')
   * </pre>
   *
   * @see https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/String/replace
   */
  public static ScriptableValue replace(final Context ctx, final Scriptable thisObj, final Object[] args,
      @Nullable Function funObj) {
    com.google.common.base.Function<Value, Value> replaceFunction
        = new com.google.common.base.Function<Value, Value>() {

      @Override
      public Value apply(Value input) {
        String stringValue = input == null ? null : input.toString();

        // Delegate to Javascript's String.replace method
        String result = (String) ScriptRuntime.checkRegExpProxy(ctx)
            .action(ctx, thisObj, ScriptRuntime.toObject(ctx, thisObj, stringValue), args, RegExpProxy.RA_REPLACE);

        return TextType.get().valueOf(result);
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
    com.google.common.base.Function<Value, Value> matchesFunction
        = new com.google.common.base.Function<Value, Value>() {

      @Override
      public Value apply(Value input) {
        String stringValue = input == null ? null : input.toString();

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
    com.google.common.base.Function<Value, Value> concatFunction = new com.google.common.base.Function<Value, Value>() {

      @Override
      public Value apply(Value input) {
        String stringValue = input == null ? null : input.toString();
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
    NativeObject valueMap = (NativeObject) args[0];

    // This could be determined by looking at the mapped values (if all ints, then 'integer', else 'text', etc.)
    ValueType returnType = TextType.get();

    Value defaultValue = defaultValue(returnType, args);
    Value nullValue = nullValue(returnType, args);

    Value currentValue = sv.getValue();
    if(currentValue.isSequence()) {
      if(currentValue.isNull()) {
        return new ScriptableValue(thisObj, returnType.nullSequence());
      }
      Collection<Value> newValues = new ArrayList<Value>();
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(lookupValue(ctx, thisObj, value, returnType, valueMap, defaultValue, nullValue));
      }
      return new ScriptableValue(thisObj, returnType.sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj,
          lookupValue(ctx, thisObj, currentValue, returnType, valueMap, defaultValue, nullValue));
    }
  }

  /**
   * Returns the default value to use when the lookup value is not found in the map. This method is used by the map()
   * method.
   *
   * @param valueType
   * @param args
   * @return
   */
  private static Value defaultValue(ValueType valueType, Object... args) {
    if(args.length < 2) {
      // No default value was specified. Return null.
      return valueType.nullValue();
    }

    Object value = args[1];
    if(value instanceof ScriptableValue) {
      return ((ScriptableValue) value).getValue();
    } else {
      return valueType.valueOf(value);
    }
  }

  /**
   * Returns the value to use when the lookup value is null. This method is used by the map() method.
   *
   * @param valueType
   * @param args
   * @return
   */
  private static Value nullValue(ValueType valueType, Object... args) {
    if(args.length < 3) {
      // No value for null was specified. Return what is defined as default value.
      return defaultValue(valueType, args);
    }

    Object value = args[2];
    if(value instanceof ScriptableValue) {
      return ((ScriptableValue) value).getValue();
    } else {
      return valueType.valueOf(value);
    }
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
  private static Value lookupValue(Context ctx, Scriptable thisObj, Value value, ValueType returnType,
      Scriptable valueMap, Value defaultValue, Value nullValue) {
    if(value.isNull()) {
      return nullValue;
    }

    // MAGMA-163: lookup using string and index-based keys
    // Lookup using a string-based key
    String asName = value.toString();
    Object newValue = valueMap.get(asName, null);
    if(newValue == NativeObject.NOT_FOUND) {
      // Not found, try converting the input to an Integer and use an indexed-lookup if it works
      Integer index = asJsIndex(value);
      if(index != null) {
        newValue = valueMap.get(index, null);
      }
    }

    if(newValue == null) {
      return returnType.nullValue();
    }

    if(newValue == NativeObject.NOT_FOUND) {
      return defaultValue;
    }

    if(newValue instanceof Function) {
      Callable valueFunction = (Callable) newValue;
      Object evaluatedValue = valueFunction
          .call(ctx, thisObj, thisObj, new Object[] { new ScriptableValue(thisObj, value) });
      if(evaluatedValue instanceof ScriptableValue) {
        newValue = ((ScriptableValue) evaluatedValue).getValue().getValue();
      } else {
        newValue = evaluatedValue;
      }
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
      com.google.common.base.Function<Value, Value> valueFunction) {

    if(sv.getValue().isNull()) {
      return sv.getValue().isSequence()
          ? new ScriptableValue(sv, TextType.get().nullSequence())
          : new ScriptableValue(sv, valueFunction.apply(sv.getValue()));
    }

    if(sv.getValue().isSequence()) {
      return new ScriptableValue(sv, TextType.get()
          .sequenceOf(Lists.newArrayList(Iterables.transform(sv.getValue().asSequence().getValue(), valueFunction))));
    }
    return new ScriptableValue(sv, valueFunction.apply(sv.getValue()));
  }

}

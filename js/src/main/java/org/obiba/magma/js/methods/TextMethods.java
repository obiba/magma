package org.obiba.magma.js.methods;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.RegExpProxy;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

/**
 * Methods of the {@code ScriptableValue} javascript class that returns {@code ScriptableValue} of {@code BooleanType}
 */
public class TextMethods {

  /**
   * <pre>
   *   $('TextVar').trim()
   * </pre>
   */
  public static ScriptableValue trim(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, TextType.get().nullValue());
    }
    String stringValue = sv.getValue().toString();
    return new ScriptableValue(thisObj, TextType.get().valueOf(stringValue.trim()));
  }

  /**
   * <pre>
   *   $('TextVar').replace('regex', '$1')
   * </pre>
   * @see https://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/String/replace
   */
  public static ScriptableValue replace(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, TextType.get().nullValue());
    }

    String stringValue = sv.getValue().toString();

    // Delegate to Javascript's String.replace method
    String result = (String) ScriptRuntime.checkRegExpProxy(ctx).action(ctx, thisObj, ScriptRuntime.toObject(ctx, thisObj, stringValue), args, RegExpProxy.RA_REPLACE);

    return new ScriptableValue(thisObj, TextType.get().valueOf(result));
  }

  /**
   * <pre>
   *   $('TextVar').matches('regex1', 'regex2', ...)
   * </pre>
   */
  public static ScriptableValue matches(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return new ScriptableValue(thisObj, TextType.get().nullValue());
    }

    String stringValue = sv.getValue().toString();

    // Delegate to Javascript's String.replace method
    boolean matches = false;
    for(Object arg : args) {
      Object result = ScriptRuntime.checkRegExpProxy(ctx).action(ctx, thisObj, ScriptRuntime.toObject(ctx, thisObj, stringValue), new Object[] { arg }, RegExpProxy.RA_MATCH);
      if(result != null) {
        matches = true;
      }
    }

    return new ScriptableValue(thisObj, BooleanType.get().valueOf(matches));
  }

  /**
   * Returns a new {@link ScriptableValue} of {@link TextType} combining the String value of this value with the String
   * values of the parameters parameters.
   * 
   * <pre>
   *   $('TextVar').concat($('TextVar'))
   *   $('Var').concat($('Var'))
   *   $('Var').concat('SomeValue')
   * </pre>
   */
  public static ScriptableValue concat(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;

    StringBuilder sb = new StringBuilder();

    sb.append(sv.toString());
    if(args != null) {
      for(Object arg : args) {
        if(arg instanceof ScriptableValue) {
          arg = arg.toString();
        }
        sb.append(arg);
      }
    }
    return new ScriptableValue(thisObj, TextType.get().valueOf(sb.toString()));
  }

  /**
   * Categorise values of a variable. That is, lookup the current value in an association table and return the
   * associated value. When the current value is not found in the association table, the method returns a null value.
   * 
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
   * @param ctx
   * @param thisObj
   * @param args
   * @param funObj
   * @return
   */
  public static ScriptableValue map(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args == null || args.length < 1 || args[0] instanceof NativeObject == false) {
      throw new MagmaJsEvaluationRuntimeException("illegal arguments to map()");
    }

    ScriptableValue sv = (ScriptableValue) thisObj;
    NativeObject valueMap = (NativeObject) args[0];

    // This could be determined by looking at the mapped values (if all ints, then 'integer', else 'text', etc.)
    ValueType returnType = TextType.get();
    Value currentValue = sv.getValue();
    if(currentValue.isSequence()) {
      List<Value> newValues = new ArrayList<Value>();
      for(Value value : currentValue.asSequence().getValue()) {
        newValues.add(lookupValue(ctx, thisObj, value, returnType, valueMap));
      }
      return new ScriptableValue(thisObj, returnType.sequenceOf(newValues));
    } else {
      return new ScriptableValue(thisObj, returnType.valueOf(lookupValue(ctx, thisObj, currentValue, returnType, valueMap)));
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
  private static Value lookupValue(Context ctx, Scriptable thisObj, Value value, ValueType returnType, NativeObject valueMap) {
    Object newValue = null;
    if(value.getValueType().isNumeric()) {
      newValue = valueMap.get(((Number) value.getValue()).intValue(), null);
    } else {
      newValue = valueMap.get((String) value.toString(), null);
    }

    if(newValue == null || newValue == NativeObject.NOT_FOUND) {
      return returnType.nullValue();
    }

    if(newValue instanceof Function) {
      Function valueFunction = (Function) newValue;
      Object evaluatedValue = valueFunction.call(ctx, thisObj, thisObj, new Object[] { new ScriptableValue(thisObj, value) });
      if(evaluatedValue instanceof ScriptableValue) {
        newValue = ((ScriptableValue) evaluatedValue).getValue().getValue();
      } else {
        newValue = evaluatedValue;
      }
    } else if(newValue instanceof Double) {
      // HACK: for rhino bug 448499: https://bugzilla.mozilla.org/show_bug.cgi?id=448499
      if(((Double) newValue).doubleValue() == 1.0d) {
        newValue = (int) 1;
      } else if(((Double) newValue).doubleValue() == 0.0d) {
        newValue = (int) 0;
      }
    }

    return returnType.valueOf(newValue);
  }
}

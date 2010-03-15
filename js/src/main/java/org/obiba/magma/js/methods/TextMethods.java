package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RegExpProxy;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
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
    String result = (String) ScriptRuntime.checkRegExpProxy(ctx).action(ctx, thisObj, ScriptRuntime.toObject(thisObj, stringValue), args, RegExpProxy.RA_REPLACE);

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
      Object result = ScriptRuntime.checkRegExpProxy(ctx).action(ctx, thisObj, ScriptRuntime.toObject(thisObj, stringValue), new Object[] { arg }, RegExpProxy.RA_MATCH);
      if(result != null) {
        matches = true;
      }
    }

    return new ScriptableValue(thisObj, BooleanType.get().valueOf(matches));
  }

  /**
   * Returns a new {@link ScriptableValue} of {@link TextType} combining the String value of the first and second
   * parameters.
   * 
   * <pre>
   *   $('TextVar').concat($('TextVar'))
   *   $('Var').concat($('Var'))
   * </pre>
   */
  public static ScriptableValue concat(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    String firstString = sv.getValue().getValue() == null ? "null" : (String) sv.getValue().getValue().toString();
    if(args != null && args.length > 0 && args[0] instanceof ScriptableValue) {
      ScriptableValue secondOperand = (ScriptableValue) args[0];
      String secondString = secondOperand.getValue().getValue() == null ? "null" : (String) secondOperand.getValue().getValue().toString();
      return new ScriptableValue(thisObj, TextType.get().valueOf(firstString + secondString));
    } else {
      return new ScriptableValue(thisObj, TextType.get().valueOf(firstString));
    }
  }
}

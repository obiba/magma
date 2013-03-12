package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.ValueType;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods of the {@code ScriptableValue} javascript class that apply to all data ValueTypes.
 */
public class ScriptableValueMethods {

  private static final Logger log = LoggerFactory.getLogger(ScriptableValueMethods.class);

  /**
   * Returns the javascript value of a {@code ScriptableValue}. Useful to turn BooleanType ScriptableValues into native
   * javascript values to be used inside if/else statements.
   * <p/>
   * <pre>
   *   if($('Admin.Interview.exportLog.destination').empty().value()) {
   *      // true
   *   } else {
   *      // false
   *   }
   * </pre>
   */
  public static Object value(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    if(sv.getValue().isNull()) {
      return null;
    }
    return sv.getDefaultValue(null);
  }

  /**
   * 1) Invoked with no arguments - ex: type()
   * <p/>
   * Returns a new {@code ScriptableValue} of type "text" containing the name of the {@code ValueType}.
   * <p/>
   * 2) Invoked with type argument - ex: type("text")
   * <p/>
   * Performs a {@code ValueType} conversion and returns a new {@code ScriptableValue} of the requested type.
   */
  public static ScriptableValue type(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableValue sv = (ScriptableValue) thisObj;
    ValueType valueType = sv.getValueType();

    // Return the ValueType name
    if(args.length == 0) {
      return new ScriptableValue(thisObj, ValueType.Factory.newValue(TextType.get(), valueType.getName()));

      // Perform a ValueType conversion
    } else {
      if(args.length > 1) {
        log.warn("{} extra parameters were passed to the javascript method.  These will be ignored.", args.length - 1);
      }
      return new ScriptableValue(thisObj, ValueType.Factory.forName(args[0].toString()).convert(sv.getValue()));
    }
  }

}

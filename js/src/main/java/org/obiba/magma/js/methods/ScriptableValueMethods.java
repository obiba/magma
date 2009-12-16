package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.js.ScriptableValue;

/**
 * Methods of the {@code ScriptableValue} javascript class that apply to all data ValueTypes.
 */
public class ScriptableValueMethods {

  /**
   * Returns the javascript value of a {@code ScriptableValue}. Useful to turn BooleanType ScriptableValues into native
   * javascript values to be used inside if/else statements.
   * 
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

}

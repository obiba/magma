package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

/**
 * Methods of the {@code ScriptableValue} javascript class that returns {@code ScriptableValue} of {@code BooleanType}
 */
public class ScriptableVariableMethods {

  public static ScriptableObject defineMethods(ScriptableObject prototype) {
    prototype.defineFunctionProperties(new String[] { "name", "attribute", "repeatable" }, ScriptableVariableMethods.class, ScriptableObject.DONTENUM);
    return prototype;
  }

  public static ScriptableValue name(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    return new ScriptableValue(thisObj, TextType.get().valueOf(sv.getVariable().getName()));
  }

  public static ScriptableValue attribute(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    return new ScriptableValue(thisObj, sv.getVariable().getAttributeValue((String) args[0]));
  }

  public static ScriptableValue repeatable(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(sv.getVariable().isRepeatable()));
  }

}

package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariableEntity;
import org.obiba.magma.type.TextType;

/**
 * Methods of the {@code ScriptableValue} javascript class that returns {@code ScriptableValue} of {@code BooleanType}
 */
public class ScriptableVariableEntityMethods {

  public static ScriptableObject defineMethods(ScriptableObject prototype) {
    prototype.defineFunctionProperties(new String[] { "type", "identifier" }, ScriptableVariableEntityMethods.class, ScriptableObject.DONTENUM);
    return prototype;
  }

  public static ScriptableValue type(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariableEntity sve = (ScriptableVariableEntity) thisObj;
    if(sve == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, TextType.get().valueOf(sve.getVariableEntity().getType()));
  }

  public static ScriptableValue identifier(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariableEntity sve = (ScriptableVariableEntity) thisObj;
    if(sve == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, TextType.get().valueOf(sve.getVariableEntity().getIdentifier()));
  }
}
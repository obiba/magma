package org.obiba.magma.js.methods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.Value;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

/**
 * Methods of the {@code ScriptableValue} javascript class that returns {@code ScriptableValue} of {@code BooleanType}
 */
public class ScriptableVariableMethods {

  public static ScriptableObject defineMethods(ScriptableObject prototype) {
    if(prototype == null) throw new IllegalArgumentException("thisObj cannot be null");
    prototype.defineFunctionProperties(new String[] { "name", "attribute", "repeatable" }, ScriptableVariableMethods.class, ScriptableObject.DONTENUM);
    return prototype;
  }

  public static ScriptableValue name(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, TextType.get().valueOf(sv.getVariable().getName()));
  }

  public static ScriptableValue type(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, TextType.get().valueOf(sv.getVariable().getValueType().getName()));
  }

  public static ScriptableValue attribute(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    String attributeName = (String) args[0];
    Value value = sv.getVariable().hasAttribute(attributeName) ? sv.getVariable().getAttributeValue(attributeName) : TextType.get().nullValue();
    return new ScriptableValue(thisObj, value);
  }

  public static ScriptableValue repeatable(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, BooleanType.get().valueOf(sv.getVariable().isRepeatable()));
  }

  public static ScriptableValue occurrenceGroup(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, TextType.get().valueOf(sv.getVariable().getOccurrenceGroup()));
  }

  public static ScriptableValue entityType(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, TextType.get().valueOf(sv.getVariable().getEntityType()));
  }

  public static ScriptableValue refEntityType(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, TextType.get().valueOf(sv.getVariable().getReferencedEntityType()));
  }

  public static ScriptableValue mimeType(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, TextType.get().valueOf(sv.getVariable().getMimeType()));
  }

  public static ScriptableValue unit(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    ScriptableVariable sv = (ScriptableVariable) thisObj;
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");
    return new ScriptableValue(thisObj, TextType.get().valueOf(sv.getVariable().getUnit()));
  }

}

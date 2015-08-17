package org.obiba.magma.js.methods;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.obiba.magma.Value;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.js.ScriptableVariable;
import org.obiba.magma.support.VariableNature;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

/**
 * Methods of the {@code ScriptableValue} javascript class that returns {@code ScriptableValue} of {@code BooleanType}
 */
public class ScriptableVariableMethods {

  private ScriptableVariableMethods() {}

  public static ScriptableValue name(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(TextType.get().valueOf(sv.getVariable().getName()));
  }

  public static ScriptableValue type(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(TextType.get().valueOf(sv.getVariable().getValueType().getName()));
  }

  public static ScriptableValue attribute(ScriptableVariable sv, Object[] args) {
    String attributeName = args[0] instanceof ScriptObjectMirror ? (String)((ScriptObjectMirror) args[0]).getSlot(0) : (String) args[0];
    Value value = sv.getVariable().hasAttribute(attributeName)
        ? sv.getVariable().getAttributeValue(attributeName)
        : TextType.get().nullValue();

    return new ScriptableValue(value);
  }

  public static ScriptableValue repeatable(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(BooleanType.get().valueOf(sv.getVariable().isRepeatable()));
  }

  public static ScriptableValue occurrenceGroup(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(TextType.get().valueOf(sv.getVariable().getOccurrenceGroup()));
  }

  public static ScriptableValue entityType(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(TextType.get().valueOf(sv.getVariable().getEntityType()));
  }

  public static ScriptableValue refEntityType(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(TextType.get().valueOf(sv.getVariable().getReferencedEntityType()));
  }

  public static ScriptableValue mimeType(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(TextType.get().valueOf(sv.getVariable().getMimeType()));
  }

  public static ScriptableValue unit(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(TextType.get().valueOf(sv.getVariable().getUnit()));
  }

  public static ScriptableValue nature(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(TextType.get().valueOf(VariableNature.getNature(sv.getVariable()).toString()));
  }

  public static ScriptableValue isNumeric(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(BooleanType.get().valueOf(sv.getVariable().getValueType().isNumeric()));
  }

  public static ScriptableValue isDateTime(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(BooleanType.get().valueOf(sv.getVariable().getValueType().isDateTime()));
  }

  public static ScriptableValue isGeo(ScriptableVariable sv, Object[] args) {
    if(sv == null) throw new IllegalArgumentException("thisObj cannot be null");

    return new ScriptableValue(BooleanType.get().valueOf(sv.getVariable().getValueType().isGeo()));
  }
}

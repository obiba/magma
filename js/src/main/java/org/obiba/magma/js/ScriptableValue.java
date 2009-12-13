package org.obiba.magma.js;

import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;

/**
 * A {@code Scriptable} implementation for {@code Value} objects.
 * <p>
 * Methods available on the {@code ScriptableValue} instances are built by the {@code ScriptableValuePrototypeFactory}.
 * It allows extending the methods of {@code ScriptableValue}.
 * @see ScriptableValuePrototypeFactory
 */
public class ScriptableValue extends ScriptableObject {

  private static final long serialVersionUID = -4342110775412157728L;

  static final String VALUE_CLASS_NAME = "Value";

  private Value value;

  /**
   * No-arg ctor for building the prototype
   */
  ScriptableValue() {

  }

  public ScriptableValue(Scriptable scope, Value value) {
    super(scope, ScriptableObject.getClassPrototype(scope, VALUE_CLASS_NAME));
    if(value == null) {
      throw new NullPointerException("values cannot be null");
    }
    this.value = value;
  }

  @Override
  public String getClassName() {
    return VALUE_CLASS_NAME;
  }

  @Override
  public Object getDefaultValue(Class<?> typeHint) {
    Value value = getValue();
    if(value.isSequence()) {
      return value.asSequence().toString();
    }
    Object defaultValue = value.getValue();
    if(value.getValueType().isDateTime()) {
      Date date = (Date) defaultValue;
      return Context.toObject(ScriptRuntime.wrapNumber(date.getTime()), this);
    }
    return defaultValue;
  }

  public Value getValue() {
    return value;
  }

  public ValueType getValueType() {
    return getValue().getValueType();
  }

  public boolean contains(Value testValue) {
    if(getValue().isSequence()) {
      return getValue().asSequence().contains(testValue);
    }
    return getValue().equals(testValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.getValue().toString();
  }
}

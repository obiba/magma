package org.obiba.magma.js;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.Value;

/**
 * A {@code Scriptable} implementation for {@code Value} objects.
 * <p>
 * Methods available on the {@code ScriptableValue} instances are built by the {@code ScriptableValuePrototypeFactory}.
 * It allows extending the methods of {@code ScriptableValue}.
 * @see ScriptableValuePrototypeFactory
 */
public class ScriptableValue extends ScriptableObject implements Iterable<Value> {

  private static final long serialVersionUID = -4342110775412157728L;

  static final String VALUE_CLASS_NAME = "Value";

  private Value[] values;

  /**
   * No-arg ctor for building the prototype
   */
  ScriptableValue() {

  }

  public ScriptableValue(Scriptable scope, Value... values) {
    super(scope, ScriptableObject.getClassPrototype(scope, VALUE_CLASS_NAME));
    if(values == null) {
      throw new NullPointerException("values cannot be null");
    }
    this.values = values;
  }

  @Override
  public String getClassName() {
    return VALUE_CLASS_NAME;
  }

  @Override
  public Object getDefaultValue(Class<?> typeHint) {
    Value value = getSingleValue();
    Object defaultValue = value.getValue();
    if(value.getValueType().isDateTime()) {
      Date date = (Date) defaultValue;
      return Context.toObject(ScriptRuntime.wrapNumber(date.getTime()), this);
    }
    return defaultValue;
  }

  public Value getSingleValue() {
    return values[0];
  }

  public Value[] getValues() {
    return values;
  }

  public int getSize() {
    return values.length;
  }

  @Override
  public Iterator<Value> iterator() {
    return Arrays.asList(values).iterator();
  }
}

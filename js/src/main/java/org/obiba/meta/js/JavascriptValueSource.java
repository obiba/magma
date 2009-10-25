package org.obiba.meta.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.meta.IValueSetReference;
import org.obiba.meta.IValueSource;
import org.obiba.meta.Initialisable;
import org.obiba.meta.MetaEngine;
import org.obiba.meta.Value;
import org.obiba.meta.ValueType;

import com.google.common.collect.Iterables;

/**
 * 
 * 
 */
public class JavascriptValueSource implements IValueSource, Initialisable {

  private ValueType type;

  private String script;

  private ScriptableObject sharedScope;

  public void setScript(String script) {
    this.script = script;
  }

  public String getScript() {
    return script;
  }

  public void setValueType(ValueType type) {
    this.type = type;
  }

  @Override
  public Value getValue(IValueSetReference valueSetReference) {
    Context ctx = Context.enter();
    Scriptable scope = ctx.newObject(sharedScope);
    scope.setPrototype(sharedScope);
    scope.setParentScope(null);
    ctx.putThreadLocal(IValueSetReference.class, valueSetReference);
    try {
      Object value = ctx.evaluateString(scope, getScript(), "source", 1, null);
      return MetaEngine.get().getValueFactory().newValue(type, value);
    } finally {
      Context.exit();
    }
  }

  @Override
  public ValueType getValueType() {
    return type;
  }

  @Override
  public void initialise() {
    Context ctx = Context.enter();
    try {
      sharedScope = ctx.initStandardObjects();
      // Register engine methods and custom methods
      sharedScope.defineFunctionProperties(Iterables.toArray(DateTimeMethods.exposedMethods, String.class), DateTimeMethods.class, ScriptableObject.DONTENUM);
    } finally {
      Context.exit();
    }
  }
}

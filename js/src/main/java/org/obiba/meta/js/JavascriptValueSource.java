package org.obiba.meta.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.meta.IValueSetReference;
import org.obiba.meta.IValueSource;
import org.obiba.meta.Value;
import org.obiba.meta.ValueFactory;
import org.obiba.meta.ValueType;

import com.google.common.collect.Iterables;

/**
 * 
 * 
 */
public class JavascriptValueSource implements IValueSource {

  private ValueType type;

  private String script;

  private ScriptableObject topLevelScope;

  public void setScript(String script) {
    this.script = script;
  }

  public void setValueType(ValueType type) {
    this.type = type;
  }

  @Override
  public Value getValue(IValueSetReference valueSetReference) {
    Context ctx = Context.enter();
    Scriptable scope = ctx.newObject(getTopLevelScope(ctx));
    scope.setPrototype(getTopLevelScope(ctx));
    scope.setParentScope(null);
    ctx.putThreadLocal(IValueSetReference.class, valueSetReference);
    try {
      Object value = ctx.evaluateString(scope, script, "source", 1, null);
      return ValueFactory.INSTANCE.newValue(type, value);
    } finally {
      Context.exit();
    }
  }

  @Override
  public ValueType getValueType() {
    return type;
  }

  protected Scriptable getTopLevelScope(Context ctx) {
    if(topLevelScope == null) {
      topLevelScope = ctx.initStandardObjects();
      topLevelScope.defineFunctionProperties(Iterables.toArray(DateTimeMethods.exposedMethods, String.class), DateTimeMethods.class, ScriptableObject.DONTENUM);
    }
    return topLevelScope;
  }
}

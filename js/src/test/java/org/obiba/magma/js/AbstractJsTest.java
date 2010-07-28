package org.obiba.magma.js;

import org.junit.After;
import org.junit.Before;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;

public abstract class AbstractJsTest {

  @Before
  public void startYourEngine() {
    newEngine().extend(new MagmaJsExtension());
    Context.enter();
  }

  @After
  public void stopYourEngine() {
    Context.exit();
    MagmaEngine.get().shutdown();
  }

  protected MagmaContext getMagmaContext() {
    return MagmaContext.asMagmaContext(Context.getCurrentContext());
  }

  protected Scriptable getSharedScope() {
    return getMagmaContext().sharedScope();
  }

  protected MagmaEngine newEngine() {
    return new MagmaEngine();
  }

  public ScriptableValue newValue(Value value) {
    return new ScriptableValue(getSharedScope(), value);
  }

  protected Object evaluate(final String script, final Variable variable) {
    return ContextFactory.getGlobal().call(new ContextAction() {
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = new ScriptableVariable(context.newLocalScope(), variable);

        final Script compiledScript = context.compileString(script, "", 1, null);
        Object value = compiledScript.exec(ctx, scope);

        return value;
      }
    });
  }

  protected ScriptableValue evaluate(final String script, final Value value) {
    return (ScriptableValue) ContextFactory.getGlobal().call(new ContextAction() {
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = newValue(value);

        final Script compiledScript = context.compileString(script, "", 1, null);
        Object value = compiledScript.exec(ctx, scope);

        return value;
      }
    });
  }

}

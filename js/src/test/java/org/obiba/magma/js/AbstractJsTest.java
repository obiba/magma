package org.obiba.magma.js;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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

  public ScriptableValue newValue(Value value, @Nullable String unit) {
    return new ScriptableValue(getSharedScope(), value, unit);
  }

  public ScriptableValue newValue(Value value) {
    return newValue(value, null);
  }

  protected Object evaluate(final String script, final Variable variable) {
    return ContextFactory.getGlobal().call(new ContextAction() {
      @Override
      public Object run(Context ctx) {
        MagmaContext context = MagmaContext.asMagmaContext(ctx);
        // Don't pollute the global scope
        Scriptable scope = new ScriptableVariable(context.newLocalScope(), variable);

        Script compiledScript = context.compileString(script, "", 1, null);

        return compiledScript.exec(ctx, scope);
      }
    });
  }

  protected ScriptableValue evaluate(String script, Value value) {
    return evaluate(script, value, null);
  }

  protected ScriptableValue evaluate(final String script, final Value value, @Nullable final String unit) {
    try {
      return (ScriptableValue) ContextFactory.getGlobal().call(new ContextAction() {
        @Override
        public Object run(Context ctx) {
          MagmaContext context = MagmaContext.asMagmaContext(ctx);
          // Don't pollute the global scope
          Scriptable scope = newValue(value, unit);

          Script compiledScript = context.compileString(script, "", 1, null);

          return compiledScript.exec(ctx, scope);
        }
      });
    } catch(WrappedException e) {
      Throwable cause = e.getWrappedException();
      if(cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw new RuntimeException(cause);
    }
  }

  protected void assertMethod(String script, Value value, Object expected) {
    ScriptableValue result = evaluate(script, value);
    assertThat(result, notNullValue());
    assertThat(result.getValue(), notNullValue());
    assertThat(result.getValue(), is(expected));
  }
}

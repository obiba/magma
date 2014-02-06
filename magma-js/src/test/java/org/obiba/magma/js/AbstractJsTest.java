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
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;

import static org.fest.assertions.api.Assertions.assertThat;

public abstract class AbstractJsTest {

  protected static final String PARTICIPANT = "Participant";

  @Before
  public void before() {
    MagmaEngine.get().shutdown();
    newEngine().extend(new MagmaJsExtension());
    Context.enter();
  }

  @After
  public void after() {
    Context.exit();
    shutdownEngine();
  }

  protected void shutdownEngine() {
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

  protected void assertMethod(String script, Value value, Value expected) {
    ScriptableValue result = evaluate(script, value);
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isNotNull();
    assertThat(result.getValue()).isEqualTo(expected);
  }

  protected static Variable createIntVariable(String name, String script) {
    return new Variable.Builder(name, IntegerType.get(), PARTICIPANT).addAttribute("script", script).build();
  }

  protected static Variable createDecimalVariable(String name, String script) {
    return new Variable.Builder(name, DecimalType.get(), PARTICIPANT).addAttribute("script", script).build();
  }
}

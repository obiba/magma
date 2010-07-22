package org.obiba.magma.js;

import org.junit.After;
import org.junit.Before;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;

public abstract class AbstractScriptableValueTest extends AbstractJsTest {

  @Before
  public void enterContext() {
    Context.enter();
  }

  @After
  public void exitContext() {
    Context.exit();
  }

  public ScriptableValue newValue(Value value) {
    return new ScriptableValue(getSharedScope(), value);
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

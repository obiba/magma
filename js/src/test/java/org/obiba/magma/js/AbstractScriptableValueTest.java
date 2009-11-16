package org.obiba.meta.js;

import org.junit.After;
import org.junit.Before;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.obiba.meta.Value;

public abstract class AbstractScriptableValueTest extends AbstractJsTest {

  @Before
  public void enterContext() {
    Context.enter();
  }

  @After
  public void exitContext() {
    Context.exit();
  }

  public Scriptable getSharedScope() {
    MagmaContext context = (MagmaContext) Context.getCurrentContext();
    return context.sharedScope();
  }

  public ScriptableValue newValue(Value... values) {
    return new ScriptableValue(getSharedScope(), values);
  }

}

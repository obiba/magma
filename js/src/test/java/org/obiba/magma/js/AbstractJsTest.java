package org.obiba.magma.js;

import org.junit.After;
import org.junit.Before;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.MagmaEngine;

public abstract class AbstractJsTest {

  @Before
  public void startYourEngine() {
    newEngine().extend(new MagmaJsExtension());
  }

  @After
  public void stopYourEngine() {
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

}

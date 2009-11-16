package org.obiba.magma.js;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.obiba.magma.MetaEngine;

public abstract class AbstractJsTest {

  @BeforeClass
  public static void prepare() {
    if(ContextFactory.hasExplicitGlobal() == false) {
      ContextFactory.initGlobal(new MagmaContextFactory());
      Context.enter().initStandardObjects();
      Context.exit();
    }
  }

  @Before
  public void startYourEngine() {
    newEngine();
  }

  @After
  public void stopYourEngine() {
    MetaEngine.get().shutdown();
  }

  protected MetaEngine newEngine() {
    return new MetaEngine();
  }

}

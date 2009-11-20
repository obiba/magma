package org.obiba.magma.js;

import org.junit.After;
import org.junit.Before;
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

  protected MagmaEngine newEngine() {
    return new MagmaEngine();
  }

}

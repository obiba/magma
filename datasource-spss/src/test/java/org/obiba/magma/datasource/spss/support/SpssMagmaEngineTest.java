package org.obiba.magma.datasource.spss.support;

import org.junit.After;
import org.junit.Before;
import org.obiba.magma.MagmaEngine;

public abstract class SpssMagmaEngineTest {

  @Before
  public void startYourEngine() {
    new MagmaEngine();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }
}

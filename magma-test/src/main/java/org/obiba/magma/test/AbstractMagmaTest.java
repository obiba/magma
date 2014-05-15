package org.obiba.magma.test;

import org.junit.After;
import org.junit.Before;
import org.obiba.magma.MagmaEngine;

public abstract class AbstractMagmaTest {

  @Before
  public void before() {
    new MagmaEngine();
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }
}

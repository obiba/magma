package org.obiba.magma;

import org.junit.After;
import org.junit.Before;

public abstract class MagmaTest {

  @Before
  public void before() {
    new MagmaEngine();
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }
}

package test;

import org.junit.After;
import org.junit.Before;
import org.obiba.magma.MagmaEngine;

//TODO REFACTOR, also used in JDBC DATASOURCE 
public abstract class AbstractMagmaTest {

  @Before
  public void startYourEngine() {
    new MagmaEngine();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }
}

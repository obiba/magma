package org.obiba.magma.datasource.spss.support;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.obiba.magma.MagmaEngine;

public abstract class SpssMagmaEngineTest {

  @BeforeClass
  public static void startYourEngine() {
    System.out.println("Starting engine");
    new MagmaEngine();
  }

  @AfterClass
  public static void stopYourEngine() {
    System.out.println("Shuttong down engine");
    MagmaEngine.get().shutdown();
  }
}

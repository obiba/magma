package org.obiba.magma.xstream;

import org.junit.After;
import org.junit.Before;
import org.obiba.magma.MagmaEngine;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractMagmaXStreamTest {

  @Before
  public void startYourEngine() {
    new MagmaEngine();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  protected XStream getDefaultXStream() {
    return new DefaultXStreamFactory().createXStream();
  }
}

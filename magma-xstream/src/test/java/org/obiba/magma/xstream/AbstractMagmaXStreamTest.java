package org.obiba.magma.xstream;

import org.obiba.magma.test.AbstractMagmaTest;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractMagmaXStreamTest extends AbstractMagmaTest {

  protected XStream getDefaultXStream() {
    return new DefaultXStreamFactory().createXStream();
  }
}

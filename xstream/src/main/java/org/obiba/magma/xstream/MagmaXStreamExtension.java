package org.obiba.magma.xstream;

import org.obiba.magma.MagmaEngineExtension;

public class MagmaXStreamExtension implements MagmaEngineExtension {

  private XStreamFactory factory = new DefaultXStreamFactory();

  @Override
  public String getName() {
    return "magma-xstream";
  }

  @Override
  public void initialise() {
  }

  public XStreamFactory getXStreamFactory() {
    return factory;
  }

}

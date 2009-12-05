package org.obiba.magma.xstream;

import java.util.Map;

import org.obiba.magma.MagmaEngineExtension;

import com.google.common.collect.ImmutableMap;

public class MagmaXStreamExtension implements MagmaEngineExtension {

  private XStreamFactory currentFactory = new DefaultXStreamFactory();

  private Map<String, ? extends XStreamFactory> compatibleFactories = ImmutableMap.of("1", currentFactory);

  @Override
  public String getName() {
    return "magma-xstream";
  }

  @Override
  public void initialise() {
  }

  public XStreamFactory getXStreamFactory() {
    return currentFactory;
  }

  public XStreamFactory getXStreamFactory(String version) {
    return compatibleFactories.get(version);
  }
}

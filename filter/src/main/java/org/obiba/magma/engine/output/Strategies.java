package org.obiba.magma.engine.output;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class Strategies {

  @XStreamImplicit(itemFieldName = "strategy")
  private final List<String> strategies;

  public Strategies(List<String> strategies) {
    this.strategies = strategies;
  }

  public List<String> getStrategies() {
    return strategies;
  }

}

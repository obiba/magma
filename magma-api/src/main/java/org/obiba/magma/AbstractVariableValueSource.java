package org.obiba.magma;

public abstract class AbstractVariableValueSource implements VariableValueSource {

  @Override
  public String getName() {
    return getVariable().getName();
  }

}

package org.obiba.magma;

import javax.validation.constraints.NotNull;

public abstract class AbstractVariableValueSource implements VariableValueSource {

  @NotNull
  @Override
  public String getName() {
    return getVariable().getName();
  }

}

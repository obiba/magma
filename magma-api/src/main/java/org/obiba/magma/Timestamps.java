package org.obiba.magma;

import javax.validation.constraints.NotNull;

public interface Timestamps {

  @NotNull
  Value getLastUpdate();

  @NotNull
  Value getCreated();

}

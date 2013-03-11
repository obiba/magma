package org.obiba.magma;

import javax.annotation.Nonnull;

public interface Timestamps {

  @Nonnull
  Value getLastUpdate();

  @Nonnull
  Value getCreated();

}

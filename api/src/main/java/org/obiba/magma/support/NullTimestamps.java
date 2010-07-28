package org.obiba.magma.support;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

public class NullTimestamps implements Timestamps {

  public static final NullTimestamps INSTANCE = new NullTimestamps();

  @Override
  public Value getCreated() {
    return DateTimeType.get().nullValue();
  }

  @Override
  public Value getLastUpdate() {
    return DateTimeType.get().nullValue();
  }

}

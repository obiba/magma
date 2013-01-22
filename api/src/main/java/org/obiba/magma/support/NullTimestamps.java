package org.obiba.magma.support;

import java.lang.ref.WeakReference;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

public class NullTimestamps implements Timestamps {

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<NullTimestamps> instance = MagmaEngine.get().registerInstance(new NullTimestamps());

  private NullTimestamps() {

  }

  public static NullTimestamps get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new NullTimestamps());
    }
    return instance.get();
  }

  @Override
  public Value getCreated() {
    return DateTimeType.get().nullValue();
  }

  @Override
  public Value getLastUpdate() {
    return DateTimeType.get().nullValue();
  }

}

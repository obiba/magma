package org.obiba.magma.support;

import java.lang.ref.WeakReference;

import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

public class NullTimestamps implements Timestamps {

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<NullTimestamps> instance = MagmaEngine.get().registerInstance(new NullTimestamps());

  private NullTimestamps() {

  }

  @SuppressWarnings("ConstantConditions")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @NotNull
  public static NullTimestamps get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new NullTimestamps());
    }
    return instance.get();
  }

  @NotNull
  @Override
  public Value getCreated() {
    return DateTimeType.get().nullValue();
  }

  @NotNull
  @Override
  public Value getLastUpdate() {
    return DateTimeType.get().nullValue();
  }

}

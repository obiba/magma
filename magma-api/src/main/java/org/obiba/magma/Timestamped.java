package org.obiba.magma;

import javax.validation.constraints.NotNull;

import com.google.common.base.Function;

public interface Timestamped {

  Function<Timestamped, Timestamps> ToTimestamps = new Function<Timestamped, Timestamps>() {

    @Override
    public Timestamps apply(Timestamped from) {
      return from == null ? null : from.getTimestamps();
    }
  };

  @NotNull
  Timestamps getTimestamps();

}

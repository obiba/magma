package org.obiba.magma;

import com.google.common.base.Function;

public interface Timestamped {

  Function<Timestamped, Timestamps> ToTimestamps = new Function<Timestamped, Timestamps>() {

    @Override
    public Timestamps apply(Timestamped from) {
      return from == null ? null : from.getTimestamps();
    }
  };

  Timestamps getTimestamps();

}

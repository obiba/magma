package org.obiba.magma;

import com.google.common.base.Function;

import javax.validation.constraints.NotNull;

public interface Timestamped {

  Function<Timestamped, Timestamps> ToTimestamps = from -> from == null ? null : from.getTimestamps();

  Function<Timestamps, Timestamped> ToTimestamped = from -> from == null ? null : (Timestamped) () -> from;

  @NotNull
  Timestamps getTimestamps();

}

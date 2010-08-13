package org.obiba.magma.transform;

import com.google.common.base.Function;

public interface BijectiveFunction<F, T> extends Function<F, T> {

  /**
   * Applies the reverse of the function defined by {@code apply}
   */
  public F unapply(T from);

}

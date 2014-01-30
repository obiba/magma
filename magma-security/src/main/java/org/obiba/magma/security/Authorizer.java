package org.obiba.magma.security;

import java.io.Serializable;
import java.util.concurrent.Callable;

public interface Authorizer extends Serializable {

  boolean isPermitted(String permission);

  <V> V silentSudo(Callable<V> sudo);

  <V> V sudo(Callable<V> sudo) throws Exception;

}

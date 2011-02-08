package org.obiba.magma.security;

import java.util.concurrent.Callable;

public interface Authorizer {

  public boolean isPermitted(String permission);

  public <V> V silentSudo(Callable<V> sudo);

  public <V> V sudo(Callable<V> sudo) throws Exception;

}

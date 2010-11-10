package org.obiba.magma.security;

public interface Authorizer {

  public boolean isPermitted(String permission);

}

package org.obiba.magma.crypt;

import java.security.PublicKey;

import org.obiba.magma.Datasource;

/**
 * A simple interface for mapping a {@code PublicKey} to a name.
 * <p>
 * A {@code KeyStore} is a type of {@code PublicKeyProvider}, since it can contain a public key mapped to an alias.
 */
public interface PublicKeyProvider {

  /**
   * Returns the {@code PublicKey} for the specified {@code name}.
   * 
   * @param name
   * @return
   */
  public PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException;

}

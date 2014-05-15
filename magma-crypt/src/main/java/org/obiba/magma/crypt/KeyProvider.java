package org.obiba.magma.crypt;

import java.security.KeyPair;
import java.security.PublicKey;

import org.obiba.magma.Datasource;

public interface KeyProvider {
  /**
   * Returns the key pair with the specified alias.
   *
   * @param alias the <code>KeyPair</code>'s alias
   * @return the <code>KeyPair</code> (<code>null</code> if not found)
   * @throws NoSuchKeyException if the requested <code>KeyPair</code> was not found
   * @throws KeyProviderSecurityException if access to the <code>KeyPair</code> was forbidden
   */
  KeyPair getKeyPair(String alias) throws NoSuchKeyException, KeyProviderSecurityException;

  /**
   * Returns the <code>KeyPair</code> for the specified public key.
   *
   * @param publicKey a public key
   * @return the corresponding <code>KeyPair</code> (<code>null</code> if not found)
   * @throws NoSuchKeyException if the requested <code>KeyPair</code> was not found
   * @throws KeyProviderSecurityException if access to the <code>KeyPair</code> was forbidden
   */
  KeyPair getKeyPair(PublicKey publicKey) throws NoSuchKeyException, KeyProviderSecurityException;

  /**
   * Returns the {@code PublicKey} for the specified {@code datasource}.
   *
   * @param name
   * @return
   */
  PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException;
}

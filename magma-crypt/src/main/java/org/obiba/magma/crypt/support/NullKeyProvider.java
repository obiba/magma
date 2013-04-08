package org.obiba.magma.crypt.support;

import java.security.KeyPair;
import java.security.PublicKey;

import org.obiba.magma.Datasource;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.crypt.KeyProviderSecurityException;
import org.obiba.magma.crypt.NoSuchKeyException;

/**
 * A {@link KeyProvider} implementation that acts as an "empty" provider (no key pairs or public keys).
 */
public class NullKeyProvider implements KeyProvider {
  //
  // KeyProvider Methods
  //

  @Override
  public KeyPair getKeyPair(String alias) throws NoSuchKeyException, KeyProviderSecurityException {
    throw new NoSuchKeyException(alias, "KeyPair not found for specified alias (" + alias + ")");
  }

  @Override
  public KeyPair getKeyPair(PublicKey publicKey) throws NoSuchKeyException, KeyProviderSecurityException {
    throw new NoSuchKeyException("KeyPair not found for specified public key");
  }

  @Override
  public PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException {
    throw new NoSuchKeyException("Public key not found for specified datasource (" + datasource.getName() + ")");
  }
}

/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.crypt.support;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import org.obiba.magma.Datasource;
import org.obiba.magma.crypt.KeyPairProvider;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.crypt.KeyProviderSecurityException;
import org.obiba.magma.crypt.NoSuchKeyException;
import org.obiba.magma.crypt.PublicKeyProvider;

/**
 * Implements both {@link PublicKeyProvider} and {@link KeyPairProvider} on top of a generated {@code KeyPair}. This
 * class is useful for testing purposes.
 */
public class GeneratedKeyPairProvider implements KeyProvider {

  /**
   * Holds the generated {@code KeyPair}
   */
  private final KeyPair keyPair;

  /**
   * Creates a new instance using the {@code RSA} algorithm.
   *
   * @throws NoSuchAlgorithmException
   */
  public GeneratedKeyPairProvider() throws NoSuchAlgorithmException {
    this("RSA");
  }

  /**
   * Creates a new instance using the specified algorithm.
   *
   * @throws NoSuchAlgorithmException
   */
  public GeneratedKeyPairProvider(String algorithm) throws NoSuchAlgorithmException {
    keyPair = KeyPairGenerator.getInstance(algorithm).generateKeyPair();
  }

  @Override
  public KeyPair getKeyPair(String alias) throws NoSuchKeyException, KeyProviderSecurityException {
    return keyPair;
  }

  @Override
  public KeyPair getKeyPair(PublicKey publicKey) throws NoSuchKeyException, KeyProviderSecurityException {
    return keyPair;
  }

  @Override
  public PublicKey getPublicKey(Datasource datasource) throws NoSuchKeyException {
    return keyPair.getPublic();
  }
}

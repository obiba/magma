/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.crypt;

import java.security.KeyPair;
import java.security.PublicKey;

/**
 * Interface for key providers.
 * <p/>
 * This is simply an abstraction of classes that can provide cryptographic keys (public or private) upon demand. (For
 * example, a keystore may serve as a key provider.)
 */
public interface KeyPairProvider {

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

}

/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.crypt;

import javax.crypto.Cipher;

/**
 * Defines the constants used to store/fetch encryption metadata.
 */
final class CipherAttributeConstants {

  /**
   * The key value for the public key attribute. Stores the public key used for wrapping the secret key.
   */
  static final String PUBLIC_KEY = "magma.crypt.publicKey";

  /**
   * The key value for the public key attribute. Stores the format that the public key entry uses.
   */
  static final String PUBLIC_KEY_FORMAT = "magma.crypt.publicKeyFormat";

  /**
   * The key value for the public key algorithm attribute. Stores the public key's algorithm.
   */
  static final String PUBLIC_KEY_ALGORITHM = "magma.crypt.publicKeyAlgorithm";

  /**
   * The key value for the secret key attribute. Stores the encrypted secret key.
   */
  static final String SECRET_KEY = "magma.crypt.secretKey";

  /**
   * The key value for the secret key's algorithm attribute. Stores the encrypted secret key's algorithm (e.g.: AES).
   */
  static final String SECRET_KEY_ALGORITHM = "magma.crypt.secretKeyAlgorithm";

  /**
   * The key value for the algorithm parameters attribute. Stores the {@code AlgorithmParameter} used by the {@code
   * Cipher} to process bytes.
   *
   * @see Cipher#getParameters()
   */
  static final String CIPHER_ALGORITHM_PARAMETERS = "magma.crypt.algorithmParameters";

  /**
   * The key value for the IV attribute. Stores the initialisation vector used by the {@code Cipher} to process bytes.
   *
   * @see Cipher#getIV()
   */
  static final String CIPHER_IV = "magma.crypt.iv";

  /**
   * The key value for the transformation string attribute. Stores the transformation used by the {@code Cipher}.
   *
   * @see Cipher#getInstance(String)
   */
  static final String CIPHER_TRANSFORMATION = "magma.crypt.transformation";

  private CipherAttributeConstants() {}
}

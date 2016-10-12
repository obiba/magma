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

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.obiba.magma.crypt.MagmaCryptRuntimeException;

/**
 * A default implementation of {@code DatasourceCipherProvider} that uses the specified {@code transformation}, {@code
 * SecretKey} and {@code AlgorithmParameters} instances to initialise {@code Cipher} instances.
 */
class DefaultDatasourceCipherFactory implements DatasourceCipherFactory {

  private final String transformation;

  private final SecretKey secretKey;

  private final AlgorithmParameters algorithmParameters;

  DefaultDatasourceCipherFactory(String transformation, SecretKey secretKey, AlgorithmParameters parameters) {
    this.transformation = transformation;
    this.secretKey = secretKey;
    algorithmParameters = parameters;
  }

  @Override
  public Cipher createDecryptingCipher() {
    try {
      Cipher cipher = Cipher.getInstance(transformation);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, algorithmParameters);
      return cipher;
    } catch(GeneralSecurityException e) {
      throw new MagmaCryptRuntimeException("Invalid parameters for decrypting Datasource.", e);
    }
  }

  @Override
  public Cipher createEncryptingCipher() {
    try {
      Cipher cipher = Cipher.getInstance(transformation);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, algorithmParameters);
      return cipher;
    } catch(GeneralSecurityException e) {
      throw new MagmaCryptRuntimeException("Invalid parameters for encrypting Datasource.", e);
    }
  }

}

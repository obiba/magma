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

  private String transformation;

  private SecretKey secretKey;

  private AlgorithmParameters algorithmParameters;

  DefaultDatasourceCipherFactory(String transformation, SecretKey secretKey, AlgorithmParameters parameters) {
    this.transformation = transformation;
    this.secretKey = secretKey;
    this.algorithmParameters = parameters;
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

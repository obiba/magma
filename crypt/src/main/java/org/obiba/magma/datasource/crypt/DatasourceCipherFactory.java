package org.obiba.magma.datasource.crypt;

import javax.crypto.Cipher;

/**
 * A {@code Cipher} factory for encrypting and decrypting datasource entries.
 */
public interface DatasourceCipherFactory {

  /**
   * Returns a newly initialised instance of a {@code Cipher} configured for encryption.
   * @return a new encrypting {@code Cipher} instance.
   */
  public Cipher createEncryptingCipher();

  /**
   * Returns a newly initialised instance of a {@code Cipher} configured for decryption.
   * @return a new decrypting {@code Cipher} instance.
   */
  public Cipher createDecryptingCipher();

}

package org.obiba.magma.datasource.fs.output;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.obiba.magma.datasource.fs.OutputStreamWrapper;

import de.schlichtherle.io.File;

public class CipherOutputStreamWrapperFactory {

  /**
   * The name of the created entry containing the {@code EncryptionData} XML.
   */
  public static final String ENCRYPTION_DATA_XML_ENTRY = "encryption.xml";

  /**
   * The key value for the public key entry in {@code EncryptionData}. Stores the public key used for wrapping the
   * secret key.
   */
  public static final String PUBLIC_KEY = "publicKey";

  /**
   * The key value for the public key format entry in {@code EncryptionData}. Stores the format that the public key
   * entry uses.
   */
  public static final String PUBLIC_KEY_FORMAT = "publicKeyFormat";

  /**
   * The key value for the public key algorithm entry in {@code EncryptionData}. Stores the public key's algorithm.
   */
  public static final String PUBLIC_KEY_ALGORITHM = "publicKeyAlgorithm";

  /**
   * The key value for the secret key entry in {@code EncryptionData}. Stores the secret key.
   */
  public static final String SECRET_KEY = "key";

  /**
   * The key value for the IV entry in {@code EncryptionData}. Stores the IV.
   */
  public static final String SECRET_KEY_IV = "iv";

  /**
   * The key value for the algorithm parameters entry in {@code EncryptionData}. Stores the {@code AlgorithmParameter}.
   */
  public static final String ALGORITHM_PARAMETERS = "algorithmParameters";

  /**
   * The key value for the transformation string entry in {@code EncryptionData}. Stores the transformation, {@see
   * Cipher#getInstance(String)}.
   */
  public static final String CIPHER_TRANSFORMATION = "transformation";

  private String algorithm = "AES";

  private String mode = "CFB";

  // CFB Mode supports no padding.
  private String padding = "NoPadding";

  // Larger key size requires installing "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy
  // Files" which can be downloaded from Sun
  private int keySize = 128;

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public void setPadding(String padding) {
    this.padding = padding;
  }

  public void setKeySize(int keySize) {
    this.keySize = keySize;
  }

  public OutputStreamWrapper createWrapper(PublicKey publicKey, File archive) {
    SecretKey sk = prepareExportKey(publicKey);
    Cipher cipher = createCipher(sk);
    return new CipherOutputStreamWrapper(cipher);
  }

  protected SecretKey prepareExportKey(PublicKey publicKey) {
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
      keyGen.init(keySize);
      SecretKey sk = keyGen.generateKey();

      byte[] keyData = wrapKey(publicKey, sk);
      // encryptionData.addEntry(SECRET_KEY, keyData);

      // Add the entry also for backward compatibility
      // chainingSupport.addEntry(ENCRYPTION_KEY_ENTRY, keyData);

      return sk;
    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  protected byte[] wrapKey(PublicKey publicKey, SecretKey sk) {
    try {

      if(publicKey.getEncoded() != null) {
        // encryptionData.addEntry(PUBLIC_KEY, publicKey.getEncoded());
        // encryptionData.addEntry(PUBLIC_KEY_FORMAT, publicKey.getFormat());
        // encryptionData.addEntry(PUBLIC_KEY_ALGORITHM, publicKey.getAlgorithm());
      }

      Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
      cipher.init(Cipher.WRAP_MODE, publicKey);
      return cipher.wrap(sk);
    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    }

  }

  protected Cipher createCipher(SecretKey sk) {
    StringBuilder transformation = new StringBuilder(algorithm);
    if(mode != null) {
      transformation.append('/').append(mode);
      if(padding != null) {
        transformation.append('/').append(padding);
      }
    }

    try {
      Cipher cipher = Cipher.getInstance(transformation.toString());
      cipher.init(Cipher.ENCRYPT_MODE, sk);

      // encryptionData.addEntry(CIPHER_TRANSFORMATION, transformation.toString());

      byte[] iv = cipher.getIV();

      // Write the IV (useful for using something else than Java to decrypt)
      if(iv != null) {
        // encryptionData.addEntry(SECRET_KEY_IV, iv);

        // Add the entry also for backward compatibility
        // chainingSupport.addEntry(ENCRYPTION_IV_ENTRY, iv);
      }

      // Write the AlgorithmParameters (useful for using Java to decrypt)
      AlgorithmParameters parameters = cipher.getParameters();
      if(parameters != null) {
        // encryptionData.addEntry(ALGORITHM_PARAMETERS, parameters.getEncoded());
      }
      return cipher;
    } catch(GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }
}

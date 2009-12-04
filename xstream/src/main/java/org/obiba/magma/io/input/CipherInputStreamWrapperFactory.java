/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.io.input;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.obiba.magma.crypt.KeyProviderSecurityException;
import org.obiba.magma.crypt.MagmaCryptRuntimeException;

import de.schlichtherle.io.File;

/**
 */
public class CipherInputStreamWrapperFactory {
  //
  // Constants
  //

  public static final String METADATA_ENTRY = "encryption.xml";

  public static final String METADATA_ENTRY_PREFIX = "encryption.";

  public static final String PKCS8_KEYSPEC_FORMAT = "PKCS#8";

  public static final String X509_KEYSPEC_FORMAT = "X.509";

  public CipherInputStreamWrapper createWrapper(PublicKey publicKey, File archive) {
    Cipher cipher = getCipher();
    return new CipherInputStreamWrapper(cipher);
  }

  /**
   * Returns a <code>Cipher</code> instance initialized for decrypting entries. The
   * <code>Cipher<code> is initialized based on the current metadata.
   * 
   * @return <code>Cipher</code> for decrypting entries
   * @throws CipherResolutionException if for any reason the requested <code>Cipher</code> could not be obtained or
   * initialized as required
   */
  private Cipher getCipher() {
    Cipher cipher = null;

    String transformation = "";// metadata.getEntry("transformation");
    String[] transformationElements = transformation.split("/");
    String algorithm = transformationElements[0];

    try {
      AlgorithmParameters algorithmParameters = getAlgorithmParameters(algorithm);
      Key unwrappedKey = getUnwrappedKey(algorithm);

      cipher = Cipher.getInstance(transformation);
      cipher.init(Cipher.DECRYPT_MODE, unwrappedKey, algorithmParameters);
    } catch(KeyProviderSecurityException ex) {
      throw ex;
    } catch(Exception e) {
      throw new MagmaCryptRuntimeException(e);
    }

    return cipher;
  }

  private AlgorithmParameters getAlgorithmParameters(String algorithm) throws IOException, NoSuchAlgorithmException {
    AlgorithmParameters algorithmParameters = null;

    // It is assumed here that there may be no algorithmParameters entry in the metadata
    // (since some algorithms may not require any).
    byte[] encodedParameters = new byte[] {};// metadata.getEntry("algorithmParameters");
    if(encodedParameters != null) {
      algorithmParameters = AlgorithmParameters.getInstance(algorithm);
      algorithmParameters.init(encodedParameters);
    }

    return algorithmParameters;
  }

  private Key getUnwrappedKey(String wrappedKeyAlgorithm) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
    byte[] wrappedKey = new byte[] {};// metadata.getEntry("key");
    Key privateKey = getPrivateKey();

    Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
    cipher.init(Cipher.UNWRAP_MODE, privateKey);
    return cipher.unwrap(wrappedKey, wrappedKeyAlgorithm, Cipher.SECRET_KEY);
  }

  private Key getPrivateKey() {
    // String publicKeyAlgorithm = metadata.getEntry("publicKeyAlgorithm");
    // String publicKeyFormat = metadata.getEntry("publicKeyFormat");
    // byte[] encodedPublicKey = metadata.getEntry("publicKey");
    // PublicKey publicKey = getPublicKey(publicKeyAlgorithm, publicKeyFormat, encodedPublicKey);

    KeyPair keyPair = null;// keyProvider.getKeyPair(publicKey);
    return keyPair.getPrivate();
  }

  private PublicKey getPublicKey(String algorithm, String format, byte[] encodedKey) {
    PublicKey publicKey = null;

    EncodedKeySpec keySpec = null;

    if(format.equals(X509_KEYSPEC_FORMAT)) {
      keySpec = new X509EncodedKeySpec(encodedKey);
    } else if(format.equals(PKCS8_KEYSPEC_FORMAT)) {
      keySpec = new PKCS8EncodedKeySpec(encodedKey);
    } else {
      // TODO: Support other formats.
      throw new RuntimeException("Unsupported KeySpec format (" + format + ")");
    }

    try {
      KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
      publicKey = keyFactory.generatePublic(keySpec);
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }

    return publicKey;
  }
}
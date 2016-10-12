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

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.obiba.magma.AttributeAware;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.Value;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.crypt.MagmaCryptRuntimeException;
import org.obiba.magma.crypt.NoSuchKeyException;

/**
 * Creates a {@link DatasourceCipherFactory} that creates {@code Cipher} instances using a {@code SecretKey} obtained
 * from the {@code Datasource} instance's attributes. The {@code SecretKey} is expected to be encrypted using a
 * {@code PublicKey} for which the instance of {@code KeyProvider} can provide the corresponding {@code KeyPair}.
 * <p/>
 * The required attributes are:
 * <ul>
 * <li>{@link CipherAttributeConstants#SECRET_KEY}</li>
 * <li>{@link CipherAttributeConstants#SECRET_KEY_ALGORITHM}</li>
 * <li>{@link CipherAttributeConstants#CIPHER_TRANSFORMATION}</li>
 * <li>{@link CipherAttributeConstants#CIPHER_ALGORITHM_PARAMETERS}</li>
 * <li>{@link CipherAttributeConstants#PUBLIC_KEY}</li>
 * <li>{@link CipherAttributeConstants#PUBLIC_KEY_FORMAT}</li>
 * <li>{@link CipherAttributeConstants#PUBLIC_KEY_ALGORITHM}</li>
 * </ul>
 *
 * @see GeneratedSecretKeyDatasourceEncryptionStrategy
 */
public class EncryptedSecretKeyDatasourceEncryptionStrategy implements DatasourceEncryptionStrategy {

  private static final String PKCS8_KEYSPEC_FORMAT = "PKCS#8";

  private static final String X509_KEYSPEC_FORMAT = "X.509";

  private transient KeyProvider keyProvider;

  //
  // DatasourceEncryptionStrategy Methods
  //

  @Override
  public void setKeyProvider(KeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  @Override
  public boolean canDecryptExistingDatasource() {
    return true;
  }

  @Override
  public DatasourceCipherFactory createDatasourceCipherFactory(Datasource ds) {
    try {
      SecretKey secretKey = getSecretKey(ds);
      String transformation = ds.getAttributeStringValue(CipherAttributeConstants.CIPHER_TRANSFORMATION);
      return new DefaultDatasourceCipherFactory(transformation, secretKey,
          getAlgorithmParameters(ds, secretKey.getAlgorithm()));
    } catch(NoSuchAttributeException e) {
      throw new MagmaCryptRuntimeException(
          "Missing metadata in Datasource '" + ds.getName() + "' to extract secret key. Expected attribute '" +
              e.getAttributeName() + "' is absent.", e);
    } catch(GeneralSecurityException e) {
      throw new MagmaCryptRuntimeException("Unable to decrypt Datasource '" + ds.getName() + "' secret key", e);
    } catch(IOException e) {
      throw new MagmaCryptRuntimeException(
          "Unexpected error while reading encryption metadata for Datasource '" + ds.getName() + "'", e);
    }
  }

  //
  // Methods
  //

  private AlgorithmParameters getAlgorithmParameters(AttributeAware datasource, String algorithm)
      throws IOException, NoSuchAlgorithmException {
    AlgorithmParameters algorithmParameters = null;

    // It is assumed here that there may be no algorithmParameters entry in the metadata
    // (since some algorithms may not require any).
    if(datasource.hasAttribute(CipherAttributeConstants.CIPHER_ALGORITHM_PARAMETERS)) {
      Value value = datasource.getAttribute(CipherAttributeConstants.CIPHER_ALGORITHM_PARAMETERS).getValue();
      algorithmParameters = AlgorithmParameters.getInstance(algorithm);
      algorithmParameters.init((byte[]) (value.isNull() ? null : value.getValue()));
    }

    return algorithmParameters;
  }

  private SecretKey getSecretKey(AttributeAware datasource)
      throws MagmaCryptRuntimeException, GeneralSecurityException {
    String algorithm = datasource.getAttributeStringValue(CipherAttributeConstants.SECRET_KEY_ALGORITHM);
    Value value = datasource.getAttribute(CipherAttributeConstants.SECRET_KEY).getValue();
    byte[] wrappedKey = (byte[]) (value.isNull() ? null : value.getValue());
    PrivateKey privateKey = getPrivateKey(datasource);

    Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
    cipher.init(Cipher.UNWRAP_MODE, privateKey);
    return (SecretKey) cipher.unwrap(wrappedKey, algorithm, Cipher.SECRET_KEY);
  }

  private PrivateKey getPrivateKey(AttributeAware datasource)
      throws NoSuchKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
    KeyPair keyPair = keyProvider.getKeyPair(getPublicKey(datasource));
    return keyPair.getPrivate();
  }

  /**
   * Extract the public key that was used to encrypt the secret key.
   *
   * @param datasource
   * @return the {@code PublicKey} used to encrypt the {@code SecretKey}
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  private PublicKey getPublicKey(AttributeAware datasource) throws NoSuchAlgorithmException, InvalidKeySpecException {
    String algorithm = datasource.getAttributeStringValue(CipherAttributeConstants.PUBLIC_KEY_ALGORITHM);
    String format = datasource.getAttributeStringValue(CipherAttributeConstants.PUBLIC_KEY_FORMAT);
    Value value = datasource.getAttribute(CipherAttributeConstants.PUBLIC_KEY).getValue();
    byte[] encodedKey = (byte[]) (value.isNull() ? null : value.getValue());

    EncodedKeySpec keySpec = getEncodedKeySpec(format, encodedKey);
    KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

    return keyFactory.generatePublic(keySpec);
  }

  private EncodedKeySpec getEncodedKeySpec(String format, byte... encodedKey) {
    switch(format) {
      case X509_KEYSPEC_FORMAT:
        return new X509EncodedKeySpec(encodedKey);
      case PKCS8_KEYSPEC_FORMAT:
        return new PKCS8EncodedKeySpec(encodedKey);
      default:
        // TODO: Support other formats.
        throw new RuntimeException("Unsupported KeySpec format (" + format + ")");
    }
  }
}

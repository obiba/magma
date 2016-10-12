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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.magma.crypt.MagmaCryptRuntimeException;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;

/**
 * Creates a {@link DatasourceCipherFactory} that creates {@code Cipher} instances using a newly generated
 * {@code SecretKey}. The secret key is encrypted using the {@code PublicKey} returned by the {@link KeyProvider}
 * instance. A {@code Cipher} instance is initialised to obtain a {@code AlgorithmParameters} instance.
 * <p/>
 * The following attributes are added to the datasource to allow decryption using the corresponding {@code PrivateKey}:
 * <ul>
 * <li>{@link CipherAttributeConstants#SECRET_KEY}</li>
 * <li>{@link CipherAttributeConstants#SECRET_KEY_ALGORITHM}</li>
 * <li>{@link CipherAttributeConstants#CIPHER_TRANSFORMATION}</li>
 * <li>{@link CipherAttributeConstants#CIPHER_ALGORITHM_PARAMETERS}</li>
 * <li>{@link CipherAttributeConstants#CIPHER_IV}</li>
 * <li>{@link CipherAttributeConstants#PUBLIC_KEY}</li>
 * <li>{@link CipherAttributeConstants#PUBLIC_KEY_FORMAT}</li>
 * <li>{@link CipherAttributeConstants#PUBLIC_KEY_ALGORITHM}</li>
 * </ul>
 */
public class GeneratedSecretKeyDatasourceEncryptionStrategy implements DatasourceEncryptionStrategy {

  private String algorithm = "AES";

  private String mode = "CFB";

  // CFB Mode supports no padding.
  private String padding = "NoPadding";

  // Larger key size requires installing "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy
  // Files" which can be downloaded from Sun
  private int keySize = 128;

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
    return false;
  }

  @Override
  public DatasourceCipherFactory createDatasourceCipherFactory(Datasource ds) {
    // If there's already a secret key in the datasource, then stop. We cannot read the contents.
    if(ds.hasAttribute(CipherAttributeConstants.SECRET_KEY)) {
      throw new MagmaCryptRuntimeException(
          "Datasource '" + ds.getName() + "' is encrypted and cannot be read without the proper decryption key.");
    }

    try {

      String transformation = getTransformation();

      SecretKey sk = getSecretKey(ds);
      AlgorithmParameters parameters = initialiseParameters(ds, transformation, sk);

      return new DefaultDatasourceCipherFactory(transformation, sk, parameters);
    } catch(GeneralSecurityException | IOException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  //
  // Methods
  //

  public void setMode(String mode) {
    this.mode = mode;
  }

  public void setPadding(String padding) {
    this.padding = padding;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public String getTransformation() {
    StringBuilder sb = new StringBuilder(algorithm);
    if(mode != null) {
      sb.append('/').append(mode);
      if(padding != null) {
        sb.append('/').append(padding);
      }
    }
    return sb.toString();

  }

  public void setKeySize(int keySize) {
    this.keySize = keySize;
  }

  private SecretKey getSecretKey(Datasource datasource) throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
    keyGen.init(keySize);
    SecretKey sk = keyGen.generateKey();

    byte[] keyData = wrapKey(datasource, sk);
    datasource.setAttributeValue(CipherAttributeConstants.SECRET_KEY, BinaryType.get().valueOf(keyData));
    datasource.setAttributeValue(CipherAttributeConstants.SECRET_KEY_ALGORITHM, TextType.get().valueOf(algorithm));
    return sk;
  }

  private AlgorithmParameters initialiseParameters(Datasource ds, String transformation, SecretKey sk)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
    // Initialise a Cipher. This causes the creation of the cipher's AlgorithmParameters.
    Cipher cipher = Cipher.getInstance(transformation);
    cipher.init(Cipher.ENCRYPT_MODE, sk);
    ds.setAttributeValue(CipherAttributeConstants.CIPHER_TRANSFORMATION, TextType.get().valueOf(transformation));
    ds.setAttributeValue(CipherAttributeConstants.CIPHER_ALGORITHM_PARAMETERS,
        BinaryType.get().valueOf(cipher.getParameters().getEncoded()));
    if(cipher.getIV() != null) {
      ds.setAttributeValue(CipherAttributeConstants.CIPHER_IV, BinaryType.get().valueOf(cipher.getIV()));
    }
    return cipher.getParameters();
  }

  private byte[] wrapKey(Datasource datasource, SecretKey sk) {

    try {
      PublicKey publicKey = keyProvider.getPublicKey(datasource);
      if(publicKey.getEncoded() != null) {
        datasource
            .setAttributeValue(CipherAttributeConstants.PUBLIC_KEY, BinaryType.get().valueOf(publicKey.getEncoded()));
        datasource.setAttributeValue(CipherAttributeConstants.PUBLIC_KEY_FORMAT,
            TextType.get().valueOf(publicKey.getFormat()));
        datasource.setAttributeValue(CipherAttributeConstants.PUBLIC_KEY_ALGORITHM,
            TextType.get().valueOf(publicKey.getAlgorithm()));
      }

      Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
      cipher.init(Cipher.WRAP_MODE, publicKey);
      return cipher.wrap(sk);
    } catch(GeneralSecurityException e) {
      throw new MagmaRuntimeException(e);
    }

  }

}

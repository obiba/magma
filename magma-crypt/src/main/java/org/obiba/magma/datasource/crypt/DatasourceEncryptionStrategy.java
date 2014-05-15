package org.obiba.magma.datasource.crypt;

import org.obiba.magma.Datasource;
import org.obiba.magma.crypt.KeyProvider;

/**
 * A strategy for encrypting a {@code Datasource}.
 */
public interface DatasourceEncryptionStrategy {
  /**
   * Sets the {@link KeyProvider} to be used by this strategy.
   *
   * @param keyProvider key provider
   */
  void setKeyProvider(KeyProvider keyProvider);

  /**
   * Returns true if this strategy is able to decrypt an existing datasource. This may return false when the strategy
   * uses a {@code PublicKey} to encrypt a datasource. Once encrypted, the datasource can only be read by the owner of
   * the corresponding {@code PrivateKey}.
   *
   * @return true when this strategy can be used to decrypt an existing datasource.
   */
  boolean canDecryptExistingDatasource();

  /**
   * Creates a new instance of {@code DatasourceCipherFactory}
   *
   * @param ds
   * @return
   */
  DatasourceCipherFactory createDatasourceCipherFactory(Datasource ds);

}

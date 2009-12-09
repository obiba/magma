package org.obiba.magma.datasource.crypt;

import org.obiba.magma.Datasource;

/**
 * A strategy for encrypting a {@code Datasource}.
 */
public interface DatasourceEncryptionStrategy {

  /**
   * Returns true if this strategy is able to decrypt an existing datasource. This may return false when the strategy
   * uses a {@code PublicKey} to encrypt a datasource. Once encrypted, the datasource can only be read by the owner of
   * the corresponding {@code PrivateKey}.
   * 
   * @return true when this strategy can be used to decrypt an existing datasource.
   */
  public boolean canDecryptExistingDatasource();

  /**
   * Creates a new instance of {@code DatasourceCipherFactory}
   * @param ds
   * @return
   */
  public DatasourceCipherFactory createDatasourceCipherFactory(Datasource ds);

}

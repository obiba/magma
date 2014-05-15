package org.obiba.magma.datasource.fs.support;

import java.io.File;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.fs.FsDatasource;

public class FsDatasourceFactory extends AbstractDatasourceFactory {

  private File file;

  private DatasourceEncryptionStrategy encryptionStrategy;

  public void setFile(File file) {
    this.file = file;
  }

  public void setEncryptionStrategy(DatasourceEncryptionStrategy encryptionStrategy) {
    this.encryptionStrategy = encryptionStrategy;
  }

  @NotNull
  @Override
  protected Datasource internalCreate() {
    FsDatasource datasource = new FsDatasource(getName(), file);
    if(encryptionStrategy != null) {
      datasource.setEncryptionStrategy(encryptionStrategy);
    }
    return datasource;
  }

}

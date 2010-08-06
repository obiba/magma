package org.obiba.magma.datasource.fs.support;

import java.io.File;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.datasource.fs.FsDatasource;

public class FsDatasourceFactory extends AbstractDatasourceFactory {

  private File file;

  public void setFile(File file) {
    this.file = file;
  }

  @Override
  protected Datasource internalCreate() {
    return new FsDatasource(getName(), file);
  }

}

/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

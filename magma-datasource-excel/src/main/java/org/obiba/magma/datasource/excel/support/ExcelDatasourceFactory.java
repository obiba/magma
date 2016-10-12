/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.excel.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.datasource.excel.ExcelDatasource;

public class ExcelDatasourceFactory extends AbstractDatasourceFactory {

  private File file;

  private boolean readOnly = false;

  public void setFile(File file) {
    this.file = file;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  @NotNull
  @Override
  protected Datasource internalCreate() {
    if(readOnly) {
      try {
        return new ExcelDatasource(getName(), new FileInputStream(file));
      } catch(FileNotFoundException e) {
        throw new MagmaRuntimeException("Exception reading excel spreadsheet " + file.getName(), e);
      }
    } else {
      return new ExcelDatasource(getName(), file);
    }
  }

}

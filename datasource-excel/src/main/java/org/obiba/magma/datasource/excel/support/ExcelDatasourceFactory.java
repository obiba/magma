package org.obiba.magma.datasource.excel.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.annotation.Nonnull;

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

  @Nonnull
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

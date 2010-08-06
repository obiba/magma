package org.obiba.magma.datasource.csv.support;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.datasource.csv.CsvDatasource;

public class CsvDatasourceFactory extends AbstractDatasourceFactory {

  private File bundle;

  private List<TableBundle> tables;

  public void setBundle(File bundle) {
    this.bundle = bundle;
  }

  public void setTables(List<TableBundle> tables) {
    this.tables = tables;
  }

  public CsvDatasourceFactory addTable(File tableDirectory) {
    if(tableDirectory != null && !hasTable(tableDirectory.getName())) {
      TableBundle bundle = new TableBundle(tableDirectory);
      getTables().add(bundle);
    }
    return this;
  }

  public CsvDatasourceFactory addTable(String name, File variables, File data) {
    if(name != null && !hasTable(name)) {
      TableBundle bundle = new TableBundle(name, variables, data);
      getTables().add(bundle);
    }
    return this;
  }

  public boolean hasTable(String name) {
    for(TableBundle bundle : getTables()) {
      if(bundle.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  public List<TableBundle> getTables() {
    return tables != null ? tables : (tables = new ArrayList<TableBundle>());
  }

  @Override
  protected Datasource internalCreate() {
    CsvDatasource datasource;
    if(bundle != null && bundle.isDirectory()) {
      datasource = new CsvDatasource(getName(), bundle);
    } else {
      datasource = new CsvDatasource(getName());
    }

    for(TableBundle tableBundle : getTables()) {
      datasource.addValueTable(tableBundle.getName(), tableBundle.getVariables(), tableBundle.getData());
    }

    return datasource;
  }
}

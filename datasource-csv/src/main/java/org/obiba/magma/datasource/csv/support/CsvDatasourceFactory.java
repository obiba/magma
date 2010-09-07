package org.obiba.magma.datasource.csv.support;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.csv.CsvDatasource;

public class CsvDatasourceFactory extends AbstractDatasourceFactory {

  private File bundle;

  private List<TableBundle> tables;

  private String characterSet;

  private String separator;

  private String quote;

  private int firstRow = 1;

  public void setBundle(File bundle) {
    this.bundle = bundle;
  }

  public void setTables(List<TableBundle> tables) {
    this.tables = tables;
  }

  public void setCharacterSet(String characterSet) {
    this.characterSet = characterSet;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  public void setQuote(String quote) {
    this.quote = quote;
  }

  public void setFirstRow(int firstRow) {
    this.firstRow = firstRow;
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

  public CsvDatasourceFactory addTable(ValueTable refTable, File data) {
    if(refTable.getName() != null && !hasTable(refTable.getName())) {
      TableBundle bundle = new TableBundle(refTable, data);
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
    if(characterSet != null) {
      datasource.setCharacterSet(characterSet);
    }
    if(quote != null) {
      datasource.setQuote(Quote.fromString(quote));
    }
    if(separator != null) {
      datasource.setSeparator(Separator.fromString(separator));
    }
    datasource.setFirstRow(firstRow);

    for(TableBundle tableBundle : getTables()) {
      if(tableBundle.hasRefTable()) {
        datasource.addValueTable(tableBundle.getRefTable(), tableBundle.getData());
      } else {
        datasource.addValueTable(tableBundle.getName(), tableBundle.getVariables(), tableBundle.getData());
      }
    }

    return datasource;
  }
}

/**
 * 
 */
package org.obiba.magma.datasource.csv.support;

import java.io.File;

import org.obiba.magma.datasource.csv.CsvDatasource;

public class TableBundle {
  private String name;

  private File variables;

  private File data;

  public TableBundle() {
    super();
  }

  public TableBundle(String name, File variables, File data) {
    super();
    this.name = name;
    this.variables = variables;
    this.data = data;
  }

  public TableBundle(File directory) {
    super();
    setDirectory(directory);
  }

  public String getName() {
    return name;
  }

  public File getVariables() {
    return variables;
  }

  public File getData() {
    return data;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setVariables(File variables) {
    this.variables = variables;
  }

  public void setData(File data) {
    this.data = data;
  }

  public void setDirectory(File directory) {
    this.name = directory.getName();
    this.variables = new File(directory, CsvDatasource.VARIABLES_FILE);
    this.data = new File(directory, CsvDatasource.DATA_FILE);
  }
}
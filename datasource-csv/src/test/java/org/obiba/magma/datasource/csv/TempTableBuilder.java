package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class TempTableBuilder {

  public final String DEFAULT_TEMP_DIRECTORY_SUFFIX = "csvTest";

  private final String tableName;

  private boolean hasData;

  private File srcDataFile;

  private File dataFile;

  private File srcVariablesFile;

  private File variablesFile;

  private boolean hasVariables;

  private String tempDirectorySuffix = DEFAULT_TEMP_DIRECTORY_SUFFIX;

  private String[] variablesHeader;

  public TempTableBuilder(String tableName) {
    this.tableName = tableName;
  }

  public TempTableBuilder addData() {
    hasData = true;
    return this;
  }

  public TempTableBuilder addData(File dataFile) {
    hasData = true;
    this.srcDataFile = dataFile;
    return this;
  }

  public TempTableBuilder addVariables(File variablesFile) {
    hasVariables = true;
    this.srcVariablesFile = variablesFile;
    return this;
  }

  public TempTableBuilder addVariables() {
    hasVariables = true;
    return this;
  }

  public TempTableBuilder setTempDirectorySuffix(String suffix) {
    this.tempDirectorySuffix = suffix;
    return this;
  }

  public File build() throws IOException {
    File tempDirectory = createTempDirectory(tempDirectorySuffix);
    File testTableDirectory = new File(tempDirectory.getAbsoluteFile(), tableName);
    testTableDirectory.mkdir();
    if(hasData) {
      dataFile = new File(testTableDirectory.getAbsoluteFile(), CsvDatasource.DATA_FILE);
      if(this.srcDataFile != null) {
        FileUtils.copyFile(this.srcDataFile, dataFile);
      } else {
        dataFile.createNewFile();
      }
    }
    if(hasVariables) {
      variablesFile = new File(testTableDirectory.getAbsoluteFile(), CsvDatasource.VARIABLES_FILE);
      if(this.srcVariablesFile != null) {
        FileUtils.copyFile(this.srcVariablesFile, variablesFile);
      } else {
        variablesFile.createNewFile();
      }
    }
    return tempDirectory;
  }

  public CsvDatasource buildCsvDatasource(String datasourceName) throws IOException {
    build();
    CsvDatasource datasource = new CsvDatasource(datasourceName).addValueTable(tableName, //
        variablesFile, //
        dataFile);
    if(variablesHeader != null) datasource.setVariablesHeader(tableName, variablesHeader);
    datasource.initialise();
    return datasource;
  }

  private File createTempDirectory(String suffix) throws IOException {
    File tempDirectory = File.createTempFile(suffix, "");
    tempDirectory.delete();
    tempDirectory.mkdir();
    return tempDirectory;
  }

  public TempTableBuilder variablesHeader(String[] variablesHeader) {
    this.variablesHeader = variablesHeader;
    return this;
  }
}

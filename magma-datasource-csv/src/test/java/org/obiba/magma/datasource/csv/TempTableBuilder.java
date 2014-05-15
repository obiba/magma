package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

@SuppressWarnings({ "ParameterHidesMemberVariable", "UnusedDeclaration" })
public class TempTableBuilder {

  public static final String DEFAULT_TEMP_DIRECTORY_SUFFIX = "csvTest";

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
    srcDataFile = dataFile;
    return this;
  }

  public TempTableBuilder addVariables(File variablesFile) {
    hasVariables = true;
    srcVariablesFile = variablesFile;
    return this;
  }

  public TempTableBuilder addVariables() {
    hasVariables = true;
    return this;
  }

  public TempTableBuilder setTempDirectorySuffix(String suffix) {
    tempDirectorySuffix = suffix;
    return this;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  public File build() throws IOException {
    File tempDirectory = createTempDirectory(tempDirectorySuffix);
    File testTableDirectory = new File(tempDirectory.getAbsoluteFile(), tableName);
    testTableDirectory.mkdir();
    if(hasData) {
      dataFile = new File(testTableDirectory.getAbsoluteFile(), CsvDatasource.DATA_FILE);
      if(srcDataFile == null) {
        dataFile.createNewFile();
      } else {
        FileUtils.copyFile(srcDataFile, dataFile);
      }
    }
    if(hasVariables) {
      variablesFile = new File(testTableDirectory.getAbsoluteFile(), CsvDatasource.VARIABLES_FILE);
      if(srcVariablesFile == null) {
        variablesFile.createNewFile();
      } else {
        FileUtils.copyFile(srcVariablesFile, variablesFile);
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

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  private File createTempDirectory(String suffix) throws IOException {
    File tempDirectory = File.createTempFile(suffix, "");
    tempDirectory.delete();
    tempDirectory.mkdir();
    return tempDirectory;
  }

  public TempTableBuilder variablesHeader(String... variablesHeader) {
    this.variablesHeader = variablesHeader;
    return this;
  }
}

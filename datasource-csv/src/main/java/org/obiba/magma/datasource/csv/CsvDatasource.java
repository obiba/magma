package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;

public class CsvDatasource extends AbstractDatasource {

  public static final String VARIABLES_FILE = "variables.csv";

  public static final String DATA_FILE = "data.csv";

  private Map<String, CsvValueTable> valueTables = new HashMap<String, CsvValueTable>();

  private String[] defaultVariablesHeader = "name#valueType#entityType#mimeType#unit#occurrenceGroup#repeatable#script".split("#");

  public CsvDatasource(String name) {
    super(name, "csv");
  }

  public CsvDatasource(String name, File bundle) {
    super(name, "csv");
    if(bundle.isDirectory()) {
      File[] directories = bundle.listFiles(new FileFilter() {

        @Override
        public boolean accept(File pathname) {
          return pathname.isDirectory();
        }
      });

      for(File dir : directories) {
        addValueTable(dir);
      }
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    return valueTables.keySet();
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return valueTables.get(tableName);
  }

  public CsvDatasource addValueTable(File tableDirectory) {
    addValueTable(tableDirectory.getName(), new File(tableDirectory, VARIABLES_FILE), new File(tableDirectory, DATA_FILE));
    return this;
  }

  public CsvDatasource addValueTable(String tableName, File variablesFile, File dataFile) {
    valueTables.put(tableName, new CsvValueTable(this, tableName, variablesFile, dataFile));
    return this;
  }

  public CsvDatasource addValueTable(ValueTable refTable, File dataFile) {
    valueTables.put(refTable.getName(), new CsvValueTable(this, refTable, dataFile));
    return this;
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    CsvValueTable valueTable = null;
    if(hasValueTable(tableName)) {
      valueTable = (CsvValueTable) getValueTable(tableName);
    } else {
      throw new MagmaRuntimeException("Cannot create writer. A table with the name " + tableName + " does not exist.");
    }
    return new CsvValueTableWriter(valueTable);
  }

  public void setVariablesHeader(String tableName, String[] header) {
    if(valueTables.containsKey(tableName)) {
      valueTables.get(tableName).setVariablesHeader(header);
    } else {
      throw new MagmaRuntimeException("Cannot set variables header. A table with the name " + tableName + " does not exist.");
    }
  }

  /**
   * Returns the variables.csv header that will be used if one was not explicitly provided for that table. This only
   * applies to new variables.csv files that are being written for the first time. Otherwise the existing header will be
   * used.
   */
  public String[] getDefaultVariablesHeader() {
    return defaultVariablesHeader;
  }

  /**
   * Set a variables.csv header that will be used by all tables that do not have a header explicitly set. This only
   * applies to new variables.csv files that are being written for the first time. Otherwise the existing header will be
   * used.
   */
  public void setDefaultVariablesHeader(String[] defaultVariablesHeader) {
    this.defaultVariablesHeader = defaultVariablesHeader;
  }

}

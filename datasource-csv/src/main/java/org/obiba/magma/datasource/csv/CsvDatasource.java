package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.csv.support.CsvDatasourceParsingException;
import org.obiba.magma.datasource.csv.support.Quote;
import org.obiba.magma.datasource.csv.support.Separator;
import org.obiba.magma.support.AbstractDatasource;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CsvDatasource extends AbstractDatasource {

  public static final String TYPE = "csv";

  public static final String VARIABLES_FILE = "variables.csv";

  public static final String DATA_FILE = "data.csv";

  public static final String DEFAULT_CHARACTER_SET = "UTF-8";

  private final Map<String, CsvValueTable> valueTables = new HashMap<String, CsvValueTable>();

  private String[] defaultVariablesHeader = "name#valueType#entityType#mimeType#unit#occurrenceGroup#repeatable#script"
      .split("#");

  private Separator separator = Separator.COMMA;

  private Quote quote = Quote.DOUBLE;

  private String characterSet = DEFAULT_CHARACTER_SET;

  private int firstRow = 1;

  public CsvDatasource(String name) {
    super(name, TYPE);
  }

  public CsvDatasource(String name, File bundle) {
    super(name, TYPE);
    if(bundle.isDirectory()) {
      File[] directories = bundle.listFiles(new FileFilter() {

        @Override
        public boolean accept(File pathName) {
          return pathName.isDirectory();
        }
      });

      for(File dir : directories) {
        addValueTable(dir);
      }
    } else {
      addValueTable(bundle.getName().substring(0, bundle.getName().lastIndexOf('.')), null, bundle);
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
    addValueTable(tableDirectory.getName(), new File(tableDirectory, VARIABLES_FILE),
        new File(tableDirectory, DATA_FILE));
    return this;
  }

  public CsvDatasource addValueTable(String tableName, @Nullable File variablesFile, @Nullable File dataFile) {
    valueTables
        .put(tableName, new CsvValueTable(this, tableName, variablesFile, dataFile, CsvValueTable.DEFAULT_ENTITY_TYPE));
    return this;
  }

  public CsvDatasource addValueTable(String tableName, File dataFile, String entityType) {
    valueTables.put(tableName, new CsvValueTable(this, tableName, dataFile, entityType));
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
      throw new CsvDatasourceParsingException(
          "Cannot create writer. A table with the name " + tableName + " does not exist.", "CsvCannotCreateWriter", 0,
          tableName);
    }
    return new CsvValueTableWriter(valueTable);
  }

  public void setVariablesHeader(String tableName, String... header) {
    if(valueTables.containsKey(tableName)) {
      valueTables.get(tableName).setVariablesHeader(header);
    } else {
      throw new CsvDatasourceParsingException(
          "Cannot set variables header. A table with the name " + tableName + " does not exist.",
          "CsvCannotSetVariableHeader", 0, tableName);
    }
  }

  /**
   * Returns the variables.csv header that will be used if one was not explicitly provided for that table. This only
   * applies to new variables.csv files that are being written for the first time. Otherwise the existing header will be
   * used.
   */
  public String[] getDefaultVariablesHeader() {
    return Arrays.copyOf(defaultVariablesHeader, defaultVariablesHeader.length);
  }

  /**
   * Set a variables.csv header that will be used by all tables that do not have a header explicitly set. This only
   * applies to new variables.csv files that are being written for the first time. Otherwise the existing header will be
   * used.
   */
  public void setDefaultVariablesHeader(String... defaultVariablesHeader) {
    this.defaultVariablesHeader = Arrays.copyOf(defaultVariablesHeader, defaultVariablesHeader.length);
  }

  CSVWriter getCsvWriter(File file) {
    return getCsvWriter(getWriter(file));
  }

  CSVWriter getCsvWriter(Writer writer) {
    return new CSVWriter(writer, separator.getCharacter(), quote.getCharacter());
  }

  Writer getWriter(File file) {
    try {
      return new OutputStreamWriter(new FileOutputStream(file, true), getCharacterSet());
    } catch(IOException e) {
      throw new CsvDatasourceParsingException("Can not get csv writer.", e, "CsvCannotObtainWriter", 0);
    }
  }

  CSVReader getCsvReader(File file) {
    return getCsvReader(getReader(file));
  }

  CSVReader getCsvReader(Reader reader) {
    return new CSVReader(reader, separator.getCharacter(), quote.getCharacter(), firstRow - 1);
  }

  CSVParser getCsvParser() {
    return new CSVParser(separator.getCharacter(), quote.getCharacter());
  }

  Reader getReader(File file) {
    try {
      return new InputStreamReader(new FileInputStream(file), getCharacterSet());
    } catch(IOException e) {
      throw new CsvDatasourceParsingException("Can not get csv reader.", e, "CsvCannotObtainReader", 0);
    }
  }

  public Separator getSeparator() {
    return separator;
  }

  public void setSeparator(Separator separator) {
    this.separator = separator;
  }

  public Quote getQuote() {
    return quote;
  }

  public void setQuote(Quote quote) {
    this.quote = quote;
  }

  public void setCharacterSet(String characterSet) {
    this.characterSet = characterSet;
  }

  public String getCharacterSet() {
    return characterSet;
  }

  public void setFirstRow(int firstRow) {
    this.firstRow = firstRow;
  }

  public int getFirstRow() {
    return firstRow;
  }

}

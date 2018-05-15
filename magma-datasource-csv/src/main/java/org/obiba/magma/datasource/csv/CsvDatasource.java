/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueType;
import org.obiba.magma.datasource.csv.support.Quote;
import org.obiba.magma.datasource.csv.support.Separator;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.DatasourceParsingException;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CsvDatasource extends AbstractDatasource {

  public static final String TYPE = "csv";

  public static final String VARIABLES_FILE = "variables.csv";

  public static final String DATA_FILE = "data.csv";

  private static final String DEFAULT_CHARACTER_SET = "UTF-8";

  private static final char DEL_CHAR = (char) 127;

  private static final String DEFAULT_VALUE_TYPE = "text";

  private static final String DEFAULT_ENTITY_ID_NAME = "entity_id";

  private final Map<String, CsvValueTable> valueTables = new HashMap<>();

  private String[] defaultVariablesHeader = "name#valueType#entityType#mimeType#unit#occurrenceGroup#repeatable#script"
      .split("#");

  private Separator separator = Separator.COMMA;

  private Quote quote = Quote.DOUBLE;

  private String characterSet = DEFAULT_CHARACTER_SET;

  private int firstRow = 1;

  private boolean multilines;

  private ValueType defaultValueType = ValueType.Factory.forName(DEFAULT_VALUE_TYPE);

  private String entityIdName;

  private Map<String,String> entityIdNames = Maps.newHashMap();

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

  @Override
  public Set<ValueTable> getValueTables() {
    return Collections.unmodifiableSet(new HashSet<ValueTable>(valueTables.values()));
  }

  public CsvDatasource addValueTable(File tableDirectory) {
    addValueTable(tableDirectory, CsvValueTable.DEFAULT_ENTITY_TYPE);
    return this;
  }

  public CsvDatasource addValueTable(File tableDirectory, String entityType) {
    addValueTable(tableDirectory.getName(), new File(tableDirectory, VARIABLES_FILE),
        new File(tableDirectory, DATA_FILE), entityType);
    return this;
  }

  public CsvDatasource addValueTable(String tableName, @Nullable File variablesFile, @Nullable File dataFile) {
    return addValueTable(tableName, variablesFile, dataFile, CsvValueTable.DEFAULT_ENTITY_TYPE);
  }

  public CsvDatasource addValueTable(String tableName, @Nullable File variablesFile, @Nullable File dataFile, String entityType) {
    valueTables
        .put(tableName, new CsvValueTable(this, tableName, variablesFile, dataFile, entityType));
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

  @NotNull
  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    if(!hasValueTable(tableName)) {
      throw new DatasourceParsingException(
          "Cannot create writer. A table with the name " + tableName + " does not exist.", "CsvCannotCreateWriter",
          tableName);
    }
    return new CsvValueTableWriter((CsvValueTable) getValueTable(tableName));
  }

  public void setVariablesHeader(String tableName, String... header) {
    if(valueTables.containsKey(tableName)) {
      valueTables.get(tableName).setVariablesHeader(header);
    } else {
      throw new DatasourceParsingException(
          "Cannot set variables header. A table with the name " + tableName + " does not exist.",
          "CsvCannotSetVariableHeader", tableName);
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

  @Nullable
  CSVWriter getCsvWriter(@Nullable File file) {
    return file == null ? null : getCsvWriter(getWriter(file));
  }

  CSVWriter getCsvWriter(Writer writer) {
    return new CSVWriter(writer, separator.getCharacter(), quote.getCharacter());
  }

  Writer getWriter(File file) {
    try {
      return new OutputStreamWriter(new FileOutputStream(file, true), getCharacterSet());
    } catch(IOException e) {
      throw new DatasourceParsingException("Can not get csv writer.", e, "CsvCannotObtainWriter");
    }
  }

  @Nullable
  CSVReader getCsvReader(@Nullable File file) {
    return file == null ? null : getCsvReader(getReader(file));
  }

  CSVReader getCsvReader(Reader reader) {
    // we don't want escape processing try DEL as a rare character until we can turn it off
    return new CSVReader(reader, separator.getCharacter(), quote.getCharacter(), DEL_CHAR, getFirstRow() - 1);
  }

  CSVParser getCsvParser() {
    // we don't want escape processing try DEL as a rare character until we can turn it off
    return new CSVParser(separator.getCharacter(), quote.getCharacter(), DEL_CHAR);
  }

  Reader getReader(File file) {
    try {
      return new InputStreamReader(new FileInputStream(file), getCharacterSet());
    } catch(IOException e) {
      throw new DatasourceParsingException("Can not get csv reader.", e, "CsvCannotObtainReader");
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

  public void setMultilines(boolean multilines) {
    this.multilines = multilines;
  }

  public boolean isMultilines() {
    return multilines;
  }

  public void setDefaultValueType(String defaultValueType) {
    if (Strings.isNullOrEmpty(defaultValueType)) return;
    try {
      this.defaultValueType = ValueType.Factory.forName(defaultValueType);
    } catch (IllegalArgumentException e) {
      this.defaultValueType = ValueType.Factory.forName(DEFAULT_VALUE_TYPE);
    }
  }

  public ValueType getDefaultValueType() {
    return defaultValueType;
  }

  /**
   * Set the default name of the column representing the entity ID, used when writing data.
   *
   * @param entityIdName
   */
  public void setEntityIdName(String entityIdName) {
    this.entityIdName = entityIdName;
  }

  private String getEntityIdName() {
    return Strings.isNullOrEmpty(entityIdName) ? DEFAULT_ENTITY_ID_NAME : entityIdName;
  }

  /**
   * Set the name of the column representing the entity ID per entity type, used when writing data.
   *
   * @param entityIdNames
   */
  public void setEntityIdNames(Map<String, String> entityIdNames) {
    this.entityIdNames = entityIdNames;
  }

  private Map<String, String> getEntityIdNames() {
    return entityIdNames == null ? Maps.newHashMap() : entityIdNames;
  }

  String getEntityIdName(String entityType) {
    return getEntityIdNames().getOrDefault(entityType, getEntityIdName());
  }
}

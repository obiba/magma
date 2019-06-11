/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.csv;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.obiba.magma.*;
import org.obiba.magma.datasource.csv.converter.VariableConverter;
import org.obiba.magma.lang.VariableEntityList;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"OverlyCoupledClass", "OverlyComplexClass"})
public class CsvValueTable extends AbstractValueTable implements Initialisable, Disposable {

  public static final String DEFAULT_ENTITY_TYPE = "Participant";

  public static final byte BLANKING_CHARACTER = ' ';

  public static final byte NEWLINE_CHARACTER = '\n';

  private static final Logger log = LoggerFactory.getLogger(CsvValueTable.class);

  private ValueTable refTable;

  @Nullable
  private File variableFile;

  @Nullable
  private final File dataFile;

  private String entityType;

  private VariableConverter variableConverter;

  final List<VariableEntity> entities = new VariableEntityList();

  private final Cache<String, List<String[]>> entityLinesBuffer = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(60, TimeUnit.SECONDS)
      .build();

  private CSVReader csvDataReader;

  private String[] currentLine;

  private boolean isVariablesFileEmpty;

  private boolean isDataFileEmpty;

  private Map<String, Integer> dataHeaderMap = new HashMap<>();

  private List<String> missingVariableNames = new ArrayList<>();

  private final CsvTimestamps timestamps;

  private boolean multilines = false;

  public CsvValueTable(Datasource datasource, String name, File dataFile, String entityType) {
    this(datasource, name, null, dataFile, entityType);
  }

  public CsvValueTable(Datasource datasource, String name, @Nullable File variableFile, @Nullable File dataFile,
                       String entityType) {
    super(datasource, name);
    this.variableFile = variableFile;
    this.dataFile = dataFile;
    this.entityType = entityType == null ? DEFAULT_ENTITY_TYPE : entityType;
    timestamps = new CsvTimestamps(variableFile, dataFile);
  }

  public CsvValueTable(Datasource datasource, ValueTable refTable, @Nullable File dataFile) {
    super(datasource, refTable.getName());
    this.refTable = refTable;
    this.dataFile = dataFile;
    entityType = refTable.getEntityType();
    timestamps = new CsvTimestamps(variableFile, dataFile);
  }

  @NotNull
  @Override
  public CsvVariableEntityProvider getVariableEntityProvider() {
    return (CsvVariableEntityProvider) super.getVariableEntityProvider();
  }

  @Override
  public synchronized ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if (!entities.contains(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }
    List<String[]> valueSetLines = getValueSetLines(entity.getIdentifier());
    if (valueSetLines == null) {
      // read as much as necessary (and cache what is skipped) from data file
      return readValueSet(entity);
    } else {
      return new CsvValueSet(this, entity, dataHeaderMap, valueSetLines);
    }
  }

  public boolean isMultilines() {
    // either detected or configured
    return multilines || getCsvDatasource().isMultilines();
  }

  @Override
  public void initialise() {
    try {
      initialiseEntities();
      initialiseVariables();
      setVariableEntityProvider(new CsvVariableEntityProvider(this, entityType));
    } catch (IOException e) {
      throw new DatasourceParsingException("Error occurred initialising csv datasource.", e, "CsvInitialisationError");
    }
  }

  @Override
  public void dispose() {
    resetCsvDataReader();
    entityLinesBuffer.invalidateAll();
  }

  @Override
  public int getVariableEntityBatchSize() {
    return ValueTable.ENTITY_BATCH_SIZE;
  }

  //
  // Private methods
  //

  /**
   * Read the value set from the data CSV file and buffer any other entities that could have been encountered.
   *
   * @param entity
   * @return
   */
  private ValueSet readValueSet(VariableEntity entity) {
    // read more
    try {
      boolean found = false;
      if (currentLine == null) {
        // skip line of headers
        getCsvDataReader().readNext();
        // read first line of data
        currentLine = getCsvDataReader().readNext();
      }
      while (!found && currentLine != null) {
        String id = getIdentifierFromCurrentLine();
        putValueSetLine(id, currentLine);
        found = entity.getIdentifier().equals(id);
        currentLine = getCsvDataReader().readNext();
        if (found && isMultilines()) {
          id = getIdentifierFromCurrentLine();
          // confirm it is found or next line is to be read as well
          found = !entity.getIdentifier().equals(id);
        }
      }
      if (currentLine == null) {
        // prepare reread from the start
        resetCsvDataReader();
      }
    } catch (IOException e) {
      throw new MagmaRuntimeException("Failed reading CSV data file", e);
    }
    List<String[]> valueSetLines = getValueSetLines(entity.getIdentifier());
    return new CsvValueSet(this, entity, dataHeaderMap, valueSetLines);
  }

  private String getIdentifierFromCurrentLine() {
    return currentLine.length > 0 ? currentLine[0] : "";
  }

  private void putValueSetLine(String id, String[] current) {
    List<String[]> lines = entityLinesBuffer.getIfPresent(id);
    if (lines == null) {
      lines = Lists.newArrayListWithExpectedSize(isMultilines() ? 10 : 1);
    }
    lines.add(current);
    entityLinesBuffer.put(id, lines);
  }

  private List<String[]> getValueSetLines(String id) {
    return entityLinesBuffer.getIfPresent(id);
  }

  private void initialiseVariables() throws IOException {
    initialiseVariablesFromDataFile();
    if (refTable == null) {
      updateDataVariablesFromVariablesFile();
    } else {
      updateDataVariablesFromRefTable();
    }
  }

  @SuppressWarnings("OverlyNestedMethod")
  private void initialiseVariablesFromDataFile() throws IOException {
    if (dataFile == null || !dataFile.exists() || dataFile.length() == 0) return;

    // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
    try (CSVReader dataHeaderReader = getCsvDataReader()) {
      String[] line = dataHeaderReader.readNext();
      if (line != null) {
        // skip first header as it's the participant ID
        for (int i = 1; i < line.length; i++) {
          String variableName = line[i].trim();
          addVariableValueSource(new CsvVariableValueSource(Variable.Builder //
              .newVariable(variableName, getCsvDatasource().getDefaultValueType(), entityType == null ? DEFAULT_ENTITY_TYPE : entityType) //
              .repeatable(isMultilines()) //
              .occurrenceGroup(isMultilines() ? getName() : null) //
              .index(i) //
              .build()));
          dataHeaderMap.put(variableName, i);
        }
        isDataFileEmpty = false;
      }
    } finally {
      resetCsvDataReader();
    }
    isVariablesFileEmpty = true;
  }

  private void updateDataVariablesFromVariablesFile() throws IOException {
    if (variableFile != null && variableFile.exists()) {
      try (CSVReader variableReader = getCsvDatasource().getCsvReader(variableFile)) {
        if (variableReader == null) return;
        initialiseVariablesFromVariablesFile(variableReader);
      }
    } else {
      initialiseVariablesFromEmptyVariablesFile();
    }
  }

  private void initialiseVariablesFromVariablesFile(CSVReader variableReader) throws IOException {
    // first line is variable headers
    String[] line = variableReader.readNext();
    if (line == null) {
      initialiseVariablesFromEmptyVariablesFile();
      return;
    }
    variableConverter = new VariableConverter(line);

    String[] nextLine = variableReader.readNext();
    while (nextLine != null) {
      if (nextLine.length <= 1) {
        nextLine = variableReader.readNext();
        continue;
      }
      Variable var = variableConverter.unmarshal(nextLine);
      entityType = var.getEntityType();

      String variableName = var.getName();

      // update only variable that was in data file
      if (hasVariable(variableName)) {
        removeVariableValueSource(variableName);
        addVariableValueSource(new CsvVariableValueSource(var));
      }
      nextLine = variableReader.readNext();
    }
  }

  private void initialiseVariablesFromEmptyVariablesFile() {
    if (variableConverter == null) {
      String[] defaultVariablesHeader = ((CsvDatasource) getDatasource()).getDefaultVariablesHeader();
      log.debug(
          "A variables.csv file or header was not explicitly provided for the table {}. Use the default header {}.",
          getName(), defaultVariablesHeader);
      variableConverter = new VariableConverter(defaultVariablesHeader);
    }
    isVariablesFileEmpty = true;
  }

  private void updateDataVariablesFromRefTable() throws IOException {
    entityType = refTable.getEntityType();
    for (Variable var : refTable.getVariables()) {
      // update only variable that was in data file
      if (hasVariable(var.getName())) {
        removeVariableValueSource(var.getName());
        addVariableValueSource(new CsvVariableValueSource(var));
      }
    }
    missingVariableNames = getMissingVariableNames();
  }

  @Nullable
  protected CSVWriter getVariableWriter() {
    return getCsvDatasource().getCsvWriter(variableFile);
  }

  @Nullable
  protected CSVWriter getValueWriter() {
    return getCsvDatasource().getCsvWriter(dataFile);
  }

  @Nullable
  File getParentFile() {
    return dataFile == null ? null : dataFile.getParentFile();
  }

  /**
   * Get the CSV reader of the data file (create it if necessary).
   *
   * @return
   */
  @SuppressWarnings("OverlyNestedMethod")
  private CSVReader getCsvDataReader() {
    if (csvDataReader == null) {
      currentLine = null;
      csvDataReader = getCsvDatasource().getCsvReader(dataFile);
      try {
        // move to the first row
        if (csvDataReader != null) for (int i = 1; i < getCsvDatasource().getFirstRow(); i++)
          csvDataReader.readNext();
      } catch (IOException e) {
        // ignore
      }
    }
    return csvDataReader;
  }

  /**
   * Close the CSV data file reader and prepare for next creation.
   */
  private void resetCsvDataReader() {
    if (csvDataReader == null) return;
    try {
      csvDataReader.close();
    } catch (IOException e) {
      // ignore
    } finally {
      csvDataReader = null;
    }
  }

  public Map<String, Integer> getDataHeaderMap() {
    return dataHeaderMap;
  }

  public String[] getDataHeaderAsArray() {
    String[] header = new String[dataHeaderMap.size() + 1];
    header[0] = getCsvDatasource().getEntityIdName(getEntityType());
    for (Map.Entry<String, Integer> entry : dataHeaderMap.entrySet()) {
      header[entry.getValue()] = entry.getKey();
    }
    return header;
  }

  public void setDataHeaderMap(Map<String, Integer> dataHeaderMap) {
    this.dataHeaderMap = dataHeaderMap;
  }

  public void setVariablesHeader(String... header) {
    variableConverter = new VariableConverter(header);
  }

  public VariableConverter getVariableConverter() {
    return variableConverter;
  }

  public boolean isVariablesFileEmpty() {
    return isVariablesFileEmpty;
  }

  public void setVariablesFileEmpty(boolean isVariablesFileEmpty) {
    this.isVariablesFileEmpty = isVariablesFileEmpty;
  }

  public boolean isDataFileEmpty() {
    return isDataFileEmpty;
  }

  public void setDataFileEmpty(boolean isDataFileEmpty) {
    this.isDataFileEmpty = isDataFileEmpty;
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return timestamps;
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    if (!entities.contains(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }
    return timestamps;
  }

  /**
   * Convenience method equivalent to {@code (CsvDatasource) getDatasource()}.
   */
  private CsvDatasource getCsvDatasource() {
    return (CsvDatasource) getDatasource();
  }

  private String getCharacterSet() {
    return getCsvDatasource().getCharacterSet();
  }

  /**
   * Read the entity identifiers from the CSV data file.
   *
   * @throws IOException
   */
  private void initialiseEntities() throws IOException {
    isDataFileEmpty = true;
    if (dataFile == null || !dataFile.exists() || dataFile.length() == 0) {
      return;
    }

    try (CSVReader dataHeaderReader = getCsvDataReader()) {
      // skip first line (headers)
      String[] line = dataHeaderReader.readNext();
      // first line(s) is headers = entity_id + variable names
      isDataFileEmpty = line == null || line.length == 0;
      buildEntitySet(dataHeaderReader);
    } finally {
      resetCsvDataReader();
    }
  }

  /**
   * Read the entity indetifiers from the non-empty CSV data lines (first field).
   *
   * @param dataHeaderReader
   * @throws IOException
   */
  private void buildEntitySet(CSVReader dataHeaderReader) throws IOException {
    String[] line;
    while ((line = dataHeaderReader.readNext()) != null) {
      if (line.length == 0) continue;
      String identifier = line[0];
      if (Strings.isNullOrEmpty(identifier)) continue;
      isDataFileEmpty = false;
      VariableEntityBean entity = new VariableEntityBean(entityType, identifier);
      if (entities.contains(entity)) {
        multilines = true;
      } else {
        entities.add(entity);
      }
    }
  }

  /**
   * Returns missing {@link Variable}s. All variables will be of the default value "text". Missing variables are created
   * for variables names specified in a csv data file that are not provided with associated {@link Variable}s when the
   * CsvValueTable is created. This happens when {@link Variable}s are provided from a reference table, and that
   * reference table does not have a {@link Variable} for every variable named in the csv data file.
   *
   * @return A collection of missing Variables.
   */
  public Iterable<Variable> getMissingVariables() {
    Collection<Variable> variables = new ArrayList<>(missingVariableNames.size());
    for (String variableName : missingVariableNames) {
      Variable.Builder variableBuilder = Variable.Builder.newVariable(variableName, TextType.get(), entityType);
      variables.add(variableBuilder.build());
    }
    return variables;
  }

  /**
   * Returns a list of variable names specified in the cvs data file for this table that do not have an associated
   * {@link Variable}. This can occur when {@code Variable}s are obtained from a reference to another table. That table
   * may not have a {@code Variable} for every variable specified in the csv data file.
   *
   * @return A list of variable names that are missing {@link Variable}s.
   * @throws IOException thrown when there is a problem reading the csv data file.
   */
  private List<String> getMissingVariableNames() throws IOException {
    List<String> missingVariables = new ArrayList<>();
    // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
    try (CSVReader dataHeaderReader = getCsvDatasource().getCsvReader(dataFile)) {
      if (dataHeaderReader != null) {
        String[] line = dataHeaderReader.readNext();
        if (line != null) {
          for (int i = 1; i < line.length; i++) {
            String variableName = line[i].trim();
            try {
              getVariableValueSource(variableName);
            } catch (NoSuchVariableException e) {
              missingVariables.add(variableName);
            }
          }
        }
      }
    }
    return missingVariables;
  }

}

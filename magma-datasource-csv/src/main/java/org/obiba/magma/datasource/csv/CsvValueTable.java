package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.csv.converter.VariableConverter;
import org.obiba.magma.datasource.csv.support.BufferedReaderEolSupport;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

@SuppressWarnings({ "OverlyCoupledClass", "OverlyComplexClass" })
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

  private CsvVariableEntityProvider variableEntityProvider;

  private String entityType;

  private VariableConverter variableConverter;

  final Set<VariableEntity> entities = new LinkedHashSet<>();

  private final Map<String, String[]> entityLinesBuffer = new LinkedHashMap<>();

  private final Map<String, CsvIndexEntry> variableNameIndex = new LinkedHashMap<>();

  private CSVReader csvDataReader;

  private boolean isLastDataCharacterNewline;

  private boolean isLastVariablesCharacterNewline;

  private boolean isVariablesFileEmpty;

  private boolean isDataFileEmpty;

  private Map<String, Integer> dataHeaderMap = new HashMap<>();

  private List<String> missingVariableNames = new ArrayList<>();

  private final CsvTimestamps timestamps;

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
  protected VariableEntityProvider getVariableEntityProvider() {
    return variableEntityProvider;
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return ImmutableSet.copyOf(variableEntityProvider.getVariableEntities());
  }

  @Override
  public synchronized ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if(!entities.contains(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }
    // check if line in in the buffer
    if(entityLinesBuffer.containsKey(entity.getIdentifier())) {
      String[] line = entityLinesBuffer.get(entity.getIdentifier());
      entityLinesBuffer.remove(entity.getIdentifier());
      return new CsvValueSet(this, entity, dataHeaderMap, line);
    }
    // read line from data file
    return readValueSet(entity);
  }

  @Override
  public void initialise() {
    try {
      initialiseVariables();
      initialiseValueSets();
      variableEntityProvider = new CsvVariableEntityProvider(this, entityType);
    } catch(IOException e) {
      throw new DatasourceParsingException("Error occurred initialising csv datasource.", e, "CsvInitialisationError");
    }
  }

  @Override
  public void dispose() {
    resetCsvDataReader();
  }

  //
  // Private methods
  //

  /**
   * Read the value set from the data CSV file and buffer any other entities that could have encountered.
   *
   * @param entity
   * @return
   */
  private ValueSet readValueSet(VariableEntity entity) {
    boolean found = false;
    String[] line = null;
    try {
      boolean firstRead = csvDataReader == null;
      String[] current = getCsvDataReader().readNext();
      // skip header
      if(firstRead) current = getCsvDataReader().readNext();
      while(current != null && !found) {
        String id = current.length > 0 ? current[0] : "";
        if(entity.getIdentifier().equals(id)) {
          line = current;
          found = true;
        } else {
          // put in the buffer
          entityLinesBuffer.put(id, current);
          current = getCsvDataReader().readNext();
        }
      }
      if(!found) {
        // re-read from the start
        resetCsvDataReader();
        return getValueSet(entity);
      }
    } catch(IOException e) {
      throw new MagmaRuntimeException("Failed reading CSV data file", e);
    }
    return new CsvValueSet(this, entity, dataHeaderMap, line);
  }

  private void initialiseVariables() throws IOException {
    initialiseVariablesFromDataFile();
    if(refTable == null) {
      if(variableFile != null && variableFile.exists()) {
        updateDataVariablesFromVariablesFile();
      }
    } else {
      updateDataVariablesFromRefTable();
    }
  }

  @SuppressWarnings("OverlyNestedMethod")
  private void initialiseVariablesFromDataFile() throws IOException {
    if(dataFile == null) return;

    // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
    try(CSVReader dataHeaderReader = getCsvDataReader()) {
      String[] line = dataHeaderReader.readNext();
      if(line != null) {
        // skip first header as it's the participant ID
        for(int i = 1; i < line.length; i++) {
          String variableName = line[i].trim();
          addVariableValueSource(new CsvVariableValueSource(Variable.Builder
              .newVariable(variableName, TextType.get(), entityType == null ? DEFAULT_ENTITY_TYPE : entityType)
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
    try(CSVReader variableReader = getCsvDatasource().getCsvReader(variableFile)) {
      if (variableReader == null) return;
      String[] line = variableReader.readNext();
      if(line == null) {
        initialiseVariablesFromEmptyFile();
      } else {
        initialiseVariablesFromLines(variableReader, line);
      }
    }
  }

  private void initialiseVariablesFromEmptyFile() {
    if(variableConverter == null) {
      String[] defaultVariablesHeader = ((CsvDatasource) getDatasource()).getDefaultVariablesHeader();
      log.debug(
          "A variables.csv file or header was not explicitly provided for the table {}. Use the default header {}.",
          getName(), defaultVariablesHeader);
      variableConverter = new VariableConverter(defaultVariablesHeader);
    }
    isVariablesFileEmpty = true;
  }

  private void initialiseVariablesFromLines(CSVReader variableReader, String... line) throws IOException {

    Map<Integer, CsvIndexEntry> lineIndex = buildVariableLineIndex();

    // first line is headers
    variableConverter = new VariableConverter(line);

    // TODO support multi line
    String[] nextLine = variableReader.readNext();
    int count = 0;
    while(nextLine != null) {
      count++;
      if(nextLine.length == 1) {
        nextLine = variableReader.readNext();
        continue;
      }
      Variable var = variableConverter.unmarshal(nextLine);
      entityType = var.getEntityType();

      String variableName = var.getName();
      variableNameIndex.put(variableName, lineIndex.get(count));

      // update only variable that was in data file
      if(hasVariable(variableName)) {
        removeVariableValueSource(variableName);
        addVariableValueSource(new CsvVariableValueSource(var));
      }
      nextLine = variableReader.readNext();
    }
  }

  private void updateDataVariablesFromRefTable() throws IOException {
    entityType = refTable.getEntityType();
    for(Variable var : refTable.getVariables()) {
      // update only variable that was in data file
      if(hasVariable(var.getName())) {
        removeVariableValueSource(var.getName());
        addVariableValueSource(new CsvVariableValueSource(var));
      }
    }
    missingVariableNames = getMissingVariableNames();
  }

  @Nullable
  CSVWriter getVariableWriter() {
    return getCsvDatasource().getCsvWriter(variableFile);
  }

  @Nullable
  CSVWriter getValueWriter() {
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
  CSVReader getCsvDataReader() {
    if(csvDataReader == null) csvDataReader = getCsvDatasource().getCsvReader(dataFile);
    return csvDataReader;
  }

  /**
   * Close the CSV data file reader and prepare for next creation.
   */
  void resetCsvDataReader() {
    if(csvDataReader == null) return;
    try {
      csvDataReader.close();
    } catch(IOException e) {
      // ignore
    } finally {
      csvDataReader = null;
    }
  }

  @NotNull
  @VisibleForTesting
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  Map<Integer, CsvIndexEntry> buildVariableLineIndex() throws IOException {
    Map<Integer, CsvIndexEntry> lineNumberMap = new LinkedHashMap<>();

    if(variableFile == null || !variableFile.exists()) {
      return lineNumberMap;
    }

    CSVParser parser = getCsvDatasource().getCsvParser();
    try(BufferedReaderEolSupport reader = new BufferedReaderEolSupport(getCsvDatasource().getReader(variableFile))) {
      int line = 0;
      int innerline = 0;
      long start = 0;
      String nextLine;
      while((nextLine = reader.readLine()) != null) {
        parser.parseLineMulti(nextLine);
        if(parser.isPending()) {
          // we are in a multiline entry
          innerline++;
        } else {
          int lineNumber = line - innerline;
          long cursorPosition = reader.getCursorPosition();

          CsvIndexEntry indexEntry = new CsvIndexEntry(start, cursorPosition);
          lineNumberMap.put(lineNumber, indexEntry);
          log.trace("[{}:{}] {}", variableFile.getName(), lineNumber, indexEntry);
          innerline = 0;
          start = cursorPosition;
        }
        line++;
      }
    }

    return lineNumberMap;
  }

  @SuppressWarnings("UnusedDeclaration")
  public boolean isLastDataCharacterNewline() {
    return isLastDataCharacterNewline;
  }

  @SuppressWarnings("UnusedDeclaration")
  public boolean isLastVariablesCharacterNewline() {
    return isLastVariablesCharacterNewline;

  }

  private long getLastByte(@NotNull File file) throws IOException {
    try(RandomAccessFile raf = new RandomAccessFile(file, "r")) {
      return raf.length();
    }
  }

  public long getVariablesLastByte() throws IOException {
    if(variableFile == null) {
      throw new MagmaRuntimeException("Cannot read last byte from null variable file for table " + getName());
    }
    return getLastByte(variableFile);
  }

  private char getLastCharacter(@NotNull File file) throws IOException {
    try(RandomAccessFile raf = new RandomAccessFile(file, "r")) {
      raf.seek(raf.length() - 1);
      return (char) raf.read();
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public char getDataLastCharacter() throws IOException {
    if(dataFile == null) {
      throw new MagmaRuntimeException("Cannot read last character from null data file for table " + getName());
    }
    return getLastCharacter(dataFile);
  }

  @SuppressWarnings("UnusedDeclaration")
  public char getVariableLastCharacter() throws IOException {
    if(variableFile == null) {
      throw new MagmaRuntimeException("Cannot read last character from null variable file for table " + getName());
    }
    return getLastCharacter(variableFile);
  }

  private void addNewline(@NotNull File file) throws IOException {
    try(RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
      raf.seek(raf.length());
      raf.writeChar(NEWLINE_CHARACTER);
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public void addDataNewline() throws IOException {
    if(dataFile == null) {
      throw new MagmaRuntimeException("Cannot write new line to null data file for table " + getName());
    }
    addNewline(dataFile);
    isLastDataCharacterNewline = true;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void addVariablesNewline() throws IOException {
    if(variableFile == null) {
      throw new MagmaRuntimeException("Cannot write new line to null variable file for table " + getName());
    }
    addNewline(variableFile);
    isLastVariablesCharacterNewline = true;
  }

  public void updateVariableIndex(Variable variable, long lastByte, String... line) {
    variableNameIndex.put(variable.getName(), new CsvIndexEntry(lastByte, lastByte + lineLength(line)));
    addVariableValueSource(new CsvVariableValueSource(variable));
  }

  private int lineLength(String... line) {
    try {
      int length = 0;
      for(String word : line) {
        if(word != null) {
          length += word.getBytes(getCharacterSet()).length + 2; // word + quote marks
        }
      }
      length += line.length - 1; // commas
      return length;
    } catch(UnsupportedEncodingException e) {
      throw new MagmaRuntimeException("Unable determine line length. ", e);
    }
  }

  public Map<String, Integer> getDataHeaderMap() {
    return dataHeaderMap;
  }

  public String[] getDataHeaderAsArray() {
    String[] header = new String[dataHeaderMap.size() + 1];
    header[0] = CsvLine.ENTITY_ID_NAME;
    for(Map.Entry<String, Integer> entry : dataHeaderMap.entrySet()) {
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
    if(!entities.contains(entity)) {
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
   * Read the entity identifiers and the field positions of the variables from the CSV data file.
   *
   * @throws IOException
   */
  private void initialiseValueSets() throws IOException {
    isDataFileEmpty = true;
    if(dataFile == null || !dataFile.exists()) {
      return;
    }

    try(CSVReader dataHeaderReader = getCsvDataReader()) {
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
    while((line = dataHeaderReader.readNext()) != null) {
      if(line.length == 0) continue;
      String identifier = line[0];
      if(Strings.isNullOrEmpty(identifier)) continue;
      isDataFileEmpty = false;
      entities.add(new VariableEntityBean(entityType, identifier));
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
    for(String variableName : missingVariableNames) {
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
    try(CSVReader dataHeaderReader = getCsvDatasource().getCsvReader(dataFile)) {
      if(dataHeaderReader != null) {
        String[] line = dataHeaderReader.readNext();
        if(line != null) {
          for(int i = 1; i < line.length; i++) {
            String variableName = line[i].trim();
            try {
              getVariableValueSource(variableName);
            } catch(NoSuchVariableException e) {
              missingVariables.add(variableName);
            }
          }
        }
      }
    }
    return missingVariables;
  }

}

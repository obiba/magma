package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

  private CSVVariableEntityProvider variableEntityProvider;

  private String entityType;

  private VariableConverter variableConverter;

  private final Map<VariableEntity, CsvIndexEntry> entityIndex = new LinkedHashMap<>();

  private final Map<String, CsvIndexEntry> variableNameIndex = new LinkedHashMap<>();

  private boolean isLastDataCharacterNewline;

  private boolean isLastVariablesCharacterNewline;

  private boolean isVariablesFileEmpty;

  private boolean isDataFileEmpty;

  private Map<String, Integer> dataHeaderMap = new HashMap<>();

  private boolean dataHeaderMapInitialized = false;

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
    timestamps = new CsvTimestamps(variableFile, dataFile);
  }

  @NotNull
  @Override
  protected VariableEntityProvider getVariableEntityProvider() {
    return variableEntityProvider;
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return Collections.unmodifiableSet(variableEntityProvider.getVariableEntities());
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    CsvIndexEntry indexEntry = entityIndex.get(entity);
    if(indexEntry == null) {
      throw new NoSuchValueSetException(this, entity);
    }
    try(Reader reader = getCsvDatasource().getReader(dataFile)) {
      CSVReader csvReader = getCsvDatasource().getCsvReader(reader);
      skipSafely(reader, indexEntry.getStart());
      return new CsvValueSet(this, entity, dataHeaderMap, csvReader.readNext());
    } catch(IOException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Override
  public void initialise() {
    try {
      initialiseVariables();
      initialiseData();
      variableEntityProvider = new CSVVariableEntityProvider(entityType);
    } catch(IOException e) {
      throw new DatasourceParsingException("Error occurred initialising csv datasource.", e, "CsvInitialisationError");
    }
  }

  @Override
  public void dispose() {
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

  private void initialiseVariablesFromDataFile() throws IOException {
    if(dataFile != null) {
      // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
      try(CSVReader dataHeaderReader = getCsvDatasource().getCsvReader(dataFile)) {
        String[] line = dataHeaderReader.readNext();
        if(line != null) {
          // skip first header as it's the participant ID
          for(int i = 1; i < line.length; i++) {
            String variableName = line[i].trim();
            addVariableValueSource(new CsvVariableValueSource(Variable.Builder
                .newVariable(variableName, TextType.get(), entityType == null ? DEFAULT_ENTITY_TYPE : entityType)
                .build()));
          }
        }
      }
    }
    isVariablesFileEmpty = true;
  }

  private void updateDataVariablesFromVariablesFile() throws IOException {
    try(CSVReader variableReader = getCsvDatasource().getCsvReader(variableFile)) {
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

  private void initialiseData() throws IOException {
    buildDataLineIndex();
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
      String nextLine = null;
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

    if(log.isTraceEnabled()) traceLineNumberMap(lineNumberMap, variableFile);

    return lineNumberMap;
  }

  @NotNull
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  Map<Integer, CsvIndexEntry> buildDataLineIndex() throws IOException {

    Map<Integer, CsvIndexEntry> lineNumberMap = new LinkedHashMap<>();
    isDataFileEmpty = true;
    if(dataFile == null || !dataFile.exists()) {
      return lineNumberMap;
    }

    CSVParser parser = getCsvDatasource().getCsvParser();

    try(BufferedReaderEolSupport reader = new BufferedReaderEolSupport(getCsvDatasource().getReader(dataFile))) {
      int line = 0;
      int innerline = 0;
      long start = 0;
      String nextLine = null;
      List<String> multiLineValues = new ArrayList<>();
      while((nextLine = reader.readLine()) != null) {
        if(isDataFileEmpty) isDataFileEmpty = false;

        String[] values = parser.parseLineMulti(nextLine);
        Collections.addAll(multiLineValues, values);
        if(parser.isPending()) {
          // we are in a multiline entry
          innerline++;
        } else {
          long cursorPosition = reader.getCursorPosition();

          int lineNumber = line - innerline;
          if(lineNumber >= getCsvDatasource().getFirstRow()) {
            log.trace("[{}:{}] {}", dataFile.getName(), lineNumber, nextLine);
            String identifier = multiLineValues.get(0);
            if(Strings.isNullOrEmpty(identifier)) {
              throw new MagmaRuntimeException(
                  "Cannot find identifier for line " + line + " in file " + dataFile.getName());
            }
            CsvIndexEntry indexEntry = new CsvIndexEntry(start, cursorPosition);
            lineNumberMap.put(lineNumber, indexEntry);
            entityIndex.put(new VariableEntityBean(entityType, identifier), indexEntry);
          } else if(!dataHeaderMapInitialized) {
            // first line(s) is headers = entity_id + variable names
            for(int i = 1; i < values.length; i++) {
              dataHeaderMap.put(values[i].trim(), i);
            }
            dataHeaderMapInitialized = true;
          }
          multiLineValues.clear();
          innerline = 0;
          start = cursorPosition;
        }
        line++;
      }
    }

    if(log.isTraceEnabled()) traceLineNumberMap(lineNumberMap, dataFile);

    return lineNumberMap;
  }

  private <T extends CsvIndexEntry> void traceLineNumberMap(Map<Integer, T> lineNumberMap, File file) {

    if(!log.isTraceEnabled()) return;

    for(Map.Entry<Integer, T> entry : lineNumberMap.entrySet()) {
      CsvIndexEntry indexEntry = entry.getValue();
      log.trace("{}: {}", entry.getKey(), indexEntry);
      try(Reader reader = getCsvDatasource().getReader(file)) {
        CSVReader csvReader = getCsvDatasource().getCsvReader(reader);
        skipSafely(reader, indexEntry.getStart());
        log.trace("   '{}'", Arrays.toString(csvReader.readNext()));
      } catch(IOException e) {
        throw new MagmaRuntimeException(e);
      }
    }
  }

  public void clear(@NotNull File file, CsvIndexEntry indexEntry) throws IOException {
    try(RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
      int length = (int) (indexEntry.getEnd() - indexEntry.getStart());
      byte[] fill = new byte[length];
      Arrays.fill(fill, BLANKING_CHARACTER);
      raf.seek(indexEntry.getStart());
      raf.write(fill);
    }
  }

  public void clearEntity(VariableEntity entity) throws IOException {
    if(dataFile == null) {
      throw new MagmaRuntimeException("Cannot write to null data file for table " + getName());
    }
    CsvIndexEntry indexEntry = entityIndex.get(entity);
    if(indexEntry != null) {
      clear(dataFile, indexEntry);
      entityIndex.remove(entity);
    }
  }

  public void clearVariable(Variable variable) throws IOException {
    if(variableFile == null) {
      throw new MagmaRuntimeException("Cannot write to null variable file for table " + getName());
    }
    CsvIndexEntry indexEntry = variableNameIndex.get(variable.getName());
    if(indexEntry != null) {
      clear(variableFile, indexEntry);
      variableNameIndex.remove(variable.getName());
      // Remove the associated VariableValueSource.
      removeVariableValueSource(variable.getName());
    }
  }

  private class CSVVariableEntityProvider implements VariableEntityProvider {

    @NotNull
    private final String entityType;

    private CSVVariableEntityProvider(@NotNull String entityType) {
      this.entityType = entityType;
    }

    @NotNull
    @Override
    public String getEntityType() {
      return entityType;
    }

    @NotNull
    @Override
    public Set<VariableEntity> getVariableEntities() {
      return entityIndex.keySet();
    }

    @Override
    public boolean isForEntityType(@SuppressWarnings("ParameterHidesMemberVariable") String entityType) {
      return getEntityType().equals(entityType);
    }

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

  public long getDataLastByte() throws IOException {
    if(dataFile == null) {
      throw new MagmaRuntimeException("Cannot read last byte from null data file for table " + getName());
    }
    return getLastByte(dataFile);
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

  public void updateDataIndex(VariableEntity entity, long lastByte, String... line) {
    log.trace("entityIndex: {}", entityIndex);
    entityIndex.put(entity, new CsvIndexEntry(lastByte, lastByte + lineLength(line)));
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
    dataHeaderMapInitialized = true;
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
    if(!entityIndex.containsKey(entity)) {
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
   * Skips {@code skip} bytes in {@code reader} and tests that that amount of byte were effectively skipped. This method
   * throws an IOException if the number of bytes actually skipped is not identical to the number of requested bytes to
   * skip.
   */
  private void skipSafely(Reader reader, long skip) throws IOException {
    if(reader.skip(skip) != skip) throw new IOException("error seeking in file");
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

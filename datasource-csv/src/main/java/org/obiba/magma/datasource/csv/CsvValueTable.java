package org.obiba.magma.datasource.csv;

import java.io.BufferedReader;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.datasource.csv.converter.VariableConverter;
import org.obiba.magma.datasource.csv.support.BufferedReaderEolSupport;
import org.obiba.magma.datasource.csv.support.CsvDatasourceParsingException;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.support.AbstractValueTable;
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

@SuppressWarnings({ "OverlyCoupledClass" })
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

  private final LinkedHashMap<VariableEntity, CsvIndexEntry> entityIndex
      = new LinkedHashMap<VariableEntity, CsvIndexEntry>();

  private final LinkedHashMap<String, CsvIndexEntry> variableNameIndex = new LinkedHashMap<String, CsvIndexEntry>();

  private boolean isLastDataCharacterNewline;

  private boolean isLastVariablesCharacterNewline;

  private boolean isVariablesFileEmpty;

  private boolean isDataFileEmpty;

  private Map<String, Integer> dataHeaderMap;

  private List<String> missingVariableNames = new ArrayList<String>();

  public CsvValueTable(Datasource datasource, String name, File dataFile, String entityType) {
    this(datasource, name, null, dataFile, entityType);
  }

  public CsvValueTable(Datasource datasource, String name, @Nullable File variableFile, @Nullable File dataFile,
      String entityType) {
    super(datasource, name);
    this.variableFile = variableFile;
    this.dataFile = dataFile;
    this.entityType = entityType == null ? DEFAULT_ENTITY_TYPE : entityType;
  }

  public CsvValueTable(Datasource datasource, ValueTable refTable, @Nullable File dataFile) {
    super(datasource, refTable.getName());
    this.refTable = refTable;
    this.dataFile = dataFile;
  }

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
    @SuppressWarnings("ConstantConditions")
    Reader reader = getCsvDatasource().getReader(dataFile);
    try {
      CSVReader csvReader = getCsvDatasource().getCsvReader(reader);
      skipSafely(reader, indexEntry.getStart());
      return new CsvValueSet(this, entity, dataHeaderMap, csvReader.readNext());
    } catch(IOException e) {
      throw new MagmaRuntimeException(e);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }

  @Override
  public void initialise() {
    try {
      initialiseVariables();
      initialiseData();
      variableEntityProvider = new CSVVariableEntityProvider(entityType);
    } catch(IOException e) {
      throw new CsvDatasourceParsingException("Error occurred initialising csv datasource.", e,
          "CsvInitialisationError", 0, (Object[]) null);
    }
  }

  @Override
  public void dispose() {
  }

  private void initialiseVariables() throws IOException {
    if(refTable == null) {
      if(variableFile != null && variableFile.exists()) {
        initialiseVariablesFromVariablesFile();
      } else {
        initialiseVariablesFromDataFile();
      }
    } else {
      initialiseVariablesFromRefTable();
    }
  }

  private void initialiseVariablesFromRefTable() throws IOException {
    entityType = refTable.getEntityType();
    for(Variable var : refTable.getVariables()) {
      addVariableValueSource(new CsvVariableValueSource(var));
    }
    missingVariableNames = getMissingVariableNames();
  }

  private void initialiseVariablesFromVariablesFile() throws IOException {
    CSVReader variableReader = getCsvDatasource().getCsvReader(variableFile);
    try {
      @SuppressWarnings("ConstantConditions")
      String[] line = variableReader.readNext();
      if(line == null) {
        initialiseVariablesFromEmptyFile();
      } else {
        initialiseVariablesFromLines(variableReader, line);
      }
    } finally {
      Closeables.closeQuietly(variableReader);
    }
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

      variableNameIndex.put(var.getName(), lineIndex.get(count));
      addVariableValueSource(new CsvVariableValueSource(var));
      nextLine = variableReader.readNext();
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

  private void initialiseVariablesFromDataFile() throws IOException {
    if(dataFile != null) {
      // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
      CSVReader dataHeaderReader = getCsvDatasource().getCsvReader(dataFile);

      @SuppressWarnings("ConstantConditions")
      String[] line = dataHeaderReader.readNext();
      dataHeaderReader.close();
      if(line != null) {
        for(int i = 1; i < line.length; i++) {
          String variableName = line[i].trim();
          Variable.Builder variableBuilder = Variable.Builder.newVariable(variableName, TextType.get(), entityType);
          addVariableValueSource(new CsvVariableValueSource(variableBuilder.build()));
        }
      }
    }
    isVariablesFileEmpty = true;
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

  @Nonnull
  @VisibleForTesting
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  Map<Integer, CsvIndexEntry> buildVariableLineIndex() throws IOException {
    Map<Integer, CsvIndexEntry> lineNumberMap = new HashMap<Integer, CsvIndexEntry>();

    if(variableFile == null || !variableFile.exists()) {
      return lineNumberMap;
    }

    CSVParser parser = getCsvDatasource().getCsvParser();
    BufferedReaderEolSupport reader = new BufferedReaderEolSupport(getCsvDatasource().getReader(variableFile));
    try {
      int line = 0;
      int innerline = 0;
      int start = 0;
      String nextLine = null;
      while((nextLine = reader.readLine()) != null) {
        parser.parseLineMulti(nextLine);
        if(parser.isPending()) {
          // we are in a multiline entry
          innerline++;
        } else {
          int lineNumber = line - innerline;
          int nextChar = reader.getNextCharPosition();
          CsvIndexEntry indexEntry = new CsvIndexEntry(start, nextChar);
          lineNumberMap.put(lineNumber, indexEntry);
          log.debug("[{}:{}] {}", variableFile.getName(), lineNumber, indexEntry);
          innerline = 0;
          start = nextChar;
        }
        line++;
      }
    } finally {
      Closeables.closeQuietly(reader);
    }

    debugLineNumberMap(lineNumberMap, variableFile);

    return lineNumberMap;
  }

  @Nonnull
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  Map<Integer, CsvIndexEntry> buildDataLineIndex() throws IOException {

    Map<Integer, CsvIndexEntry> lineNumberMap = new HashMap<Integer, CsvIndexEntry>();
    isDataFileEmpty = true;
    if(dataFile == null || !dataFile.exists()) {
      return lineNumberMap;
    }

    CSVParser parser = getCsvDatasource().getCsvParser();
    BufferedReaderEolSupport reader = new BufferedReaderEolSupport(getCsvDatasource().getReader(dataFile));
    try {
      int line = 0;
      int innerline = 0;
      int start = 0;
      String nextLine = null;
      List<String> multiLineValues = new ArrayList<String>();
      while((nextLine = reader.readLine()) != null) {
        if(isDataFileEmpty) isDataFileEmpty = false;

        String[] values = parser.parseLineMulti(nextLine);
        Collections.addAll(multiLineValues, values);
        if(parser.isPending()) {
          // we are in a multiline entry
          innerline++;
        } else {
          int nextChar = reader.getNextCharPosition();

          if(dataHeaderMap == null) {
            // first line is headers = entity_id + variable names
            dataHeaderMap = new HashMap<String, Integer>();
            for(int i = 1; i < values.length; i++) {
              dataHeaderMap.put(values[i].trim(), i);
            }
          } else {
            int lineNumber = line - innerline;
            log.debug("[{}:{}] {}", dataFile.getName(), lineNumber, nextLine);
            String identifier = multiLineValues.get(0);
            if(Strings.isNullOrEmpty(identifier)) {
              throw new MagmaRuntimeException(
                  "Cannot find identifier for line " + line + " in file " + dataFile.getName());
            }
            CsvIndexEntry indexEntry = new CsvIndexEntry(start, nextChar);
            lineNumberMap.put(lineNumber, indexEntry);
            entityIndex.put(new VariableEntityBean(entityType, identifier), indexEntry);
          }
          multiLineValues.clear();
          innerline = 0;
          start = nextChar;
        }
        line++;
      }
    } finally {
      Closeables.closeQuietly(reader);
    }

    debugLineNumberMap(lineNumberMap, dataFile);

    return lineNumberMap;
  }

  private <T extends CsvIndexEntry> void debugLineNumberMap(Map<Integer, T> lineNumberMap, File file) {
    if(!log.isDebugEnabled()) return;
    log.debug("lineNumberMap: {}", lineNumberMap);
    for(Map.Entry<Integer, T> entry : lineNumberMap.entrySet()) {
      CsvIndexEntry indexEntry = entry.getValue();
      log.debug("{}: {}", entry.getKey(), indexEntry);
      Reader reader = getCsvDatasource().getReader(file);
      try {
        skipSafely(reader, indexEntry.getStart());
        log.debug("   '{}'", new BufferedReader(reader).readLine());
      } catch(IOException e) {
        throw new MagmaRuntimeException(e);
      } finally {
        Closeables.closeQuietly(reader);
      }
    }
  }

  public void clear(@Nullable File file, CsvIndexEntry indexEntry) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "rws");
    try {
      int length = (int) (indexEntry.getEnd() - indexEntry.getStart());
      byte[] fill = new byte[length];
      Arrays.fill(fill, BLANKING_CHARACTER);
      raf.seek(indexEntry.getStart());
      raf.write(fill);
    } finally {
      Closeables.closeQuietly(raf);
    }
  }

  public void clearEntity(VariableEntity entity) throws IOException {
    CsvIndexEntry indexEntry = entityIndex.get(entity);
    if(indexEntry != null) {
      clear(dataFile, indexEntry);
      entityIndex.remove(entity);
    }
  }

  public void clearVariable(Variable variable) throws IOException {
    CsvIndexEntry indexEntry = variableNameIndex.get(variable.getName());
    if(indexEntry != null) {
      clear(variableFile, indexEntry);
      variableNameIndex.remove(variable.getName());
      // Remove the associated VariableValueSource.
      VariableValueSource vvs = getVariableValueSource(variable.getName());
      getSources().remove(vvs);
    }
  }

  private class CSVVariableEntityProvider implements VariableEntityProvider {

    private final String entityType;

    private CSVVariableEntityProvider(String entityType) {
      this.entityType = entityType;
    }

    @Override
    public String getEntityType() {
      return entityType;
    }

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

  private long getLastByte(@Nullable File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    try {
      return raf.length();
    } finally {
      Closeables.closeQuietly(raf);
    }
  }

  public long getDataLastByte() throws IOException {
    return getLastByte(dataFile);
  }

  public long getVariablesLastByte() throws IOException {
    return getLastByte(variableFile);
  }

  private char getLastCharacter(@Nullable File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    try {
      raf.seek(raf.length() - 1);
      return (char) raf.read();
    } finally {
      Closeables.closeQuietly(raf);
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public char getDataLastCharacter() throws IOException {
    return getLastCharacter(dataFile);
  }

  @SuppressWarnings("UnusedDeclaration")
  public char getVariableLastCharacter() throws IOException {
    return getLastCharacter(variableFile);
  }

  private void addNewline(@Nullable File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "rws");
    try {
      raf.seek(raf.length());
      raf.writeChar(NEWLINE_CHARACTER);
    } finally {
      Closeables.closeQuietly(raf);
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public void addDataNewline() throws IOException {
    addNewline(dataFile);
    isLastDataCharacterNewline = true;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void addVariablesNewline() throws IOException {
    addNewline(variableFile);
    isLastVariablesCharacterNewline = true;
  }

  public void updateDataIndex(VariableEntity entity, long lastByte, String... line) {
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

  @Override
  public Timestamps getTimestamps() {
    return new CsvTimestamps(variableFile, dataFile);
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
    Collection<Variable> variables = new ArrayList<Variable>(missingVariableNames.size());
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
    List<String> missingVariables = new ArrayList<String>();
    // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
    CSVReader dataHeaderReader = getCsvDatasource().getCsvReader(dataFile);
    if(dataHeaderReader != null) {
      String[] line = dataHeaderReader.readNext();
      dataHeaderReader.close();
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
    return missingVariables;
  }

}

package org.obiba.magma.datasource.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
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
import org.obiba.magma.datasource.csv.support.CsvDatasourceParsingException;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

@SuppressWarnings({ "UnusedDeclaration", "OverlyCoupledClass" })
public class CsvValueTable extends AbstractValueTable implements Initialisable, Disposable {

  public static final String DEFAULT_ENTITY_TYPE = "Participant";

  public static final byte BLANKING_CHARACTER = ' ';

  public static final byte NEWLINE_CHARACTER = '\n';

  private static final Logger log = LoggerFactory.getLogger(CsvValueTable.class);

  private ValueTable refTable;

  private File variableFile;

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

  public CsvValueTable(Datasource datasource, String name, File variableFile, File dataFile, String entityType) {
    super(datasource, name);
    this.variableFile = variableFile;
    this.dataFile = dataFile;
    this.entityType = entityType == null ? DEFAULT_ENTITY_TYPE : entityType;
  }

  public CsvValueTable(Datasource datasource, ValueTable refTable, File dataFile) {
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
    if(indexEntry != null) {
      FileInputStream fis = null;
      try {
        InputStreamReader fr = new InputStreamReader(fis = new FileInputStream(dataFile), getCharacterSet());
        CSVReader csvReader = getCsvDatasource().getCsvReader(fr);
        skipSafely(fis, indexEntry.getStart());
        return new CsvValueSet(this, entity, dataHeaderMap, csvReader.readNext());
      } catch(IOException e) {
        throw new MagmaRuntimeException(e);
      } finally {
        Closeables.closeQuietly(fis);
      }
    } else {
      throw new NoSuchValueSetException(this, entity);
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
          "CsvInitialisationError", 0, null);
    }
  }

  @Override
  public void dispose() {
  }

  private void initialiseVariables() throws IOException {
    if(refTable != null) {
      initialiseVariablesFromRefTable();
    } else if(variableFile != null && variableFile.exists()) {
      initialiseVariablesFromVariablesFile();
    } else {
      initialiseVariablesFromDataFile();
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
    Map<Integer, CsvIndexEntry> lineIndex = buildLineIndex(variableFile);

    CSVReader variableReader = getCsvDatasource().getCsvReader(variableFile);
    try {
      String[] line = variableReader.readNext();

      if(line != null) {
        // first line is headers
        variableConverter = new VariableConverter(line);

        // TODO support multi line
        line = variableReader.readNext();
        int count = 0;
        while(line != null) {
          count++;
          if(line.length == 1) {
            line = variableReader.readNext();
            continue;
          }
          Variable var = variableConverter.unmarshal(line);
          entityType = var.getEntityType();

          variableNameIndex.put(var.getName(), lineIndex.get(count));
          addVariableValueSource(new CsvVariableValueSource(var));
          line = variableReader.readNext();
        }
      } else {
        if(variableConverter == null) {
          String[] defaultVariablesHeader = ((CsvDatasource) getDatasource()).getDefaultVariablesHeader();
          log.debug(
              "A variables.csv file or header was not explicitly provided for the table {}. Use the default header {}.",
              getName(), defaultVariablesHeader);
          variableConverter = new VariableConverter(defaultVariablesHeader);
        }
        isVariablesFileEmpty = true;
      }
    } finally {
      Closeables.closeQuietly(variableReader);
    }
  }

  private void initialiseVariablesFromDataFile() throws IOException {
    if(dataFile != null) {
      // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
      CSVReader dataHeaderReader = getCsvDatasource().getCsvReader(dataFile);

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

  CSVWriter getVariableWriter() {
    return getCsvDatasource().getCsvWriter(variableFile);
  }

  CSVWriter getValueWriter() {
    return getCsvDatasource().getCsvWriter(dataFile);
  }

  File getParentFile() {
    return dataFile.getParentFile();
  }

  private void initialiseData() throws IOException {
    if(dataFile != null) {
      Map<Integer, CsvIndexEntry> lineIndex = buildLineIndex(dataFile);

      CSVParser parser = getCsvDatasource().getCsvParser();
      BufferedReader reader = new BufferedReader(getCsvDatasource().getReader(dataFile));
      try {

        String nextLine = reader.readLine();
        String[] line = parser.parseLine(nextLine);
        // first line is headers = entity_id + variable names
        Map<String, Integer> headerMap = new HashMap<String, Integer>();
        if(line != null) {
          for(int i = 1; i < line.length; i++) {
            headerMap.put(line[i].trim(), i);
          }
        } else {
          isDataFileEmpty = true;
        }
        dataHeaderMap = headerMap;

        int count = 1;
        nextLine = reader.readLine();
        boolean wasPending = false;
        while(nextLine != null) {
          line = parser.parseLineMulti(nextLine);
          if(wasPending == false && line.length > 0 && line[0] != null && line[0].trim().length() > 0) {
            VariableEntity entity = new VariableEntityBean(entityType, line[0]);
            entityIndex.put(entity, lineIndex.get(count));
          }
          wasPending = parser.isPending();
          count++;
          nextLine = reader.readLine();
        }
      } finally {
        Closeables.closeQuietly(reader);
      }

    } else {
      isDataFileEmpty = true;
    }
  }

  @VisibleForTesting
  Map<Integer, CsvIndexEntry> buildLineIndex(File file) throws IOException {
    Map<Integer, CsvIndexEntry> lineNumberMap = new HashMap<Integer, CsvIndexEntry>();

    CSVParser parser = getCsvDatasource().getCsvParser();
    BufferedReader reader = new BufferedReader(getCsvDatasource().getReader(file));
    try {
      int line = 0;
      int innerline = 0;
      int strt = 0;
      int nd = 0;
      String nextLine = reader.readLine();
      while(nextLine != null) {
        nd += nextLine.getBytes(getCharacterSet()).length;
        parser.parseLineMulti(nextLine);
        if(parser.isPending()) {
          // we are in a multiline entry
          innerline++;
          nd++;
        } else {
          log.debug("[{}:{}] {}", file.getName(), line - innerline, nextLine);
          lineNumberMap.put(line - innerline, new CsvIndexEntry(strt, nd));
          innerline = 0;
          nd++;
          strt = nd;
        }
        line++;
        nextLine = reader.readLine();
      }
    } finally {
      Closeables.closeQuietly(reader);
    }

    return lineNumberMap;
  }

  public void clear(File file, CsvIndexEntry indexEntry) throws IOException {
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
    public boolean isForEntityType(String entityType) {
      return getEntityType().equals(entityType);
    }

  }

  public boolean isLastDataCharacterNewline() {
    return isLastDataCharacterNewline;
  }

  public boolean isLastVariablesCharacterNewline() {
    return isLastVariablesCharacterNewline;

  }

  private long getLastByte(File file) throws IOException {
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

  private char getLastCharacter(File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    try {
      raf.seek(raf.length() - 1);
      return (char) raf.read();
    } finally {
      Closeables.closeQuietly(raf);
    }
  }

  public char getDataLastCharacter() throws IOException {
    return getLastCharacter(dataFile);
  }

  public char getVariableLastCharacter() throws IOException {
    return getLastCharacter(variableFile);
  }

  private void addNewline(File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "rws");
    try {
      raf.seek(raf.length());
      raf.writeChar(NEWLINE_CHARACTER);
    } finally {
      Closeables.closeQuietly(raf);
    }
  }

  public void addDataNewline() throws IOException {
    addNewline(dataFile);
    isLastDataCharacterNewline = true;
  }

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
   * Skips {@code skip} bytes in {@code is} and tests that that amount of byte were effectively skipped. This method
   * throws an IOException if the number of bytes actually skipped is not identical to the number of requested bytes to
   * skip.
   */
  private void skipSafely(InputStream is, long skip) throws IOException {
    if(is.skip(skip) != skip) throw new IOException("error seeking in file");
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
    if(dataFile != null) {
      // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
      CSVReader dataHeaderReader = getCsvDatasource().getCsvReader(dataFile);
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

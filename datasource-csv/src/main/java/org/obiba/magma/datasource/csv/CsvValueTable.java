package org.obiba.magma.datasource.csv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.datasource.csv.converter.VariableConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.annotations.VisibleForTesting;

public class CsvValueTable extends AbstractValueTable implements Initialisable, Disposable {

  public static final String DEFAULT_ENTITY_TYPE = "Participant";

  public static final byte BLANKING_CHARACTER = ' ';

  public static final byte NEWLINE_CHARACTER = '\n';

  private static final Logger log = LoggerFactory.getLogger(CsvValueTable.class);

  private ValueTable refTable;

  private File variableFile;

  private File dataFile;

  private CSVReader variableReader;

  private CSVReader dataReader;

  private CSVVariableEntityProvider variableEntityProvider;

  private String entityType;

  private VariableConverter variableConverter;

  private LinkedHashMap<VariableEntity, CsvIndexEntry> entityIndex = new LinkedHashMap<VariableEntity, CsvIndexEntry>();

  private LinkedHashMap<String, CsvIndexEntry> variableNameIndex = new LinkedHashMap<String, CsvIndexEntry>();

  private boolean isLastDataCharacterNewline;

  private boolean isLastVariablesCharacterNewline;

  private boolean isVariablesFileEmpty;

  private boolean isDataFileEmpty;

  private Map<String, Integer> dataHeaderMap;

  public CsvValueTable(CsvDatasource datasource, String name, File variableFile, File dataFile) {
    super(datasource, name);
    this.variableFile = variableFile;
    this.dataFile = dataFile;
  }

  public CsvValueTable(CsvDatasource datasource, ValueTable refTable, File dataFile) {
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
      try {
        FileInputStream fis;
        InputStreamReader fr = new InputStreamReader(fis = new FileInputStream(dataFile), CsvDatasource.UTF8);
        CSVReader csvReader = getCsvDatasource().getCsvReader(fr);
        fis.skip(indexEntry.getStart());
        return new CsvValueSet(this, entity, dataHeaderMap, csvReader.readNext());
      } catch(IOException e) {
        throw new MagmaRuntimeException(e);
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
    } catch(IOException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Override
  public void dispose() {
    try {
      if(dataReader != null) dataReader.close();
      if(variableReader != null) variableReader.close();
    } catch(IOException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  private void initialiseVariables() throws IOException {
    if(refTable != null) {
      entityType = refTable.getEntityType();
      for(Variable var : refTable.getVariables()) {
        addVariableValueSource(new CsvVariableValueSource(var));
      }
    } else if(variableFile != null) {
      Map<Integer, CsvIndexEntry> lineIndex = buildLineIndex(variableFile);
      variableReader = getCsvDatasource().getCsvReader(variableFile);

      String[] line = variableReader.readNext();

      if(line != null) {
        // first line is headers
        variableConverter = new VariableConverter(line);

        line = variableReader.readNext();
        int count = 0;
        while(line != null) {
          count++;
          if(line.length == 1) {
            line = variableReader.readNext();
            continue;
          }
          Variable var = variableConverter.unmarshal(line);
          if(entityType == null) entityType = var.getEntityType();

          variableNameIndex.put(var.getName(), lineIndex.get(count));
          addVariableValueSource(new CsvIndexVariableValueSource(var.getName()));
          line = variableReader.readNext();
        }
      } else {
        if(variableConverter == null) {
          String[] defaultVariablesHeader = ((CsvDatasource) getDatasource()).getDefaultVariablesHeader();
          log.debug("A variables.csv file or header was not explicitly provided for the table {}. Use the default header {}.", getName(), defaultVariablesHeader);
          variableConverter = new VariableConverter(defaultVariablesHeader);
        }
        isVariablesFileEmpty = true;
      }
    } else {
      entityType = DEFAULT_ENTITY_TYPE;
      if(dataFile != null) {
        // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
        CSVReader dataHeaderReader = getCsvDatasource().getCsvReader(dataFile);

        String[] line = dataHeaderReader.readNext();
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
  }

  CSVWriter getVariableWriter() {
    return getCsvDatasource().getCsvWriter(variableFile);
  }

  CSVWriter getValueWriter() {
    return getCsvDatasource().getCsvWriter(dataFile);
  }

  private void initialiseData() throws IOException {
    if(dataFile != null) {
      Map<Integer, CsvIndexEntry> lineIndex = buildLineIndex(dataFile);

      dataReader = getCsvDatasource().getCsvReader(dataFile);

      String[] line = dataReader.readNext();
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

      int count = 0;
      line = dataReader.readNext();
      while(line != null) {
        count++;
        if(line.length == 1) {
          line = dataReader.readNext();
          continue;
        }
        VariableEntity entity = new VariableEntityBean(entityType, line[0]);
        entityIndex.put(entity, lineIndex.get(count));

        line = dataReader.readNext();
      }

      variableEntityProvider = new CSVVariableEntityProvider(entityType);
    } else {
      isDataFileEmpty = true;
    }
  }

  @VisibleForTesting
  Map<Integer, CsvIndexEntry> buildLineIndex(File file) throws IOException {
    Map<Integer, CsvIndexEntry> lineNumberMap = new HashMap<Integer, CsvIndexEntry>();
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
    int counter = 0;
    int lineCounter = 0;
    int b;
    int previousCharacter = -1;
    int start = 0;
    int end = 0;
    while((b = bis.read()) != -1) {
      counter++;
      if(b == NEWLINE_CHARACTER) {
        end = counter - 1;
        lineNumberMap.put(lineCounter, new CsvIndexEntry(start, end));
        start = end + 1;
        // break;
        lineCounter++;
      }
      previousCharacter = b;
    }
    if(previousCharacter != NEWLINE_CHARACTER) {
      lineNumberMap.put(lineCounter, new CsvIndexEntry(start, counter));
    }
    return lineNumberMap;
  }

  public void clearEntity(VariableEntity entity) throws IOException {
    CsvIndexEntry indexEntry = entityIndex.get(entity);
    if(indexEntry != null) {
      RandomAccessFile raf = new RandomAccessFile(dataFile, "rws");
      int length = (int) (indexEntry.getEnd() - indexEntry.getStart());
      byte[] fill = new byte[length];
      Arrays.fill(fill, BLANKING_CHARACTER);
      raf.seek(indexEntry.getStart());
      raf.write(fill);
      entityIndex.remove(entity);
      raf.close();
    }
  }

  public void clearVariable(Variable variable) throws IOException {
    CsvIndexEntry indexEntry = variableNameIndex.get(variable.getName());
    if(indexEntry != null) {
      RandomAccessFile raf = new RandomAccessFile(variableFile, "rws");
      int length = (int) (indexEntry.getEnd() - indexEntry.getStart());
      byte[] fill = new byte[length];
      Arrays.fill(fill, BLANKING_CHARACTER);
      raf.seek(indexEntry.getStart());
      raf.write(fill);
      variableNameIndex.remove(variable.getName());
      raf.close();
    }
  }

  private class CSVVariableEntityProvider implements VariableEntityProvider {

    private String entityType;

    public CSVVariableEntityProvider(String entityType) {
      super();
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

  private class CsvIndexVariableValueSource implements VariableValueSource {

    private final String variableName;

    public CsvIndexVariableValueSource(String variableName) {
      this.variableName = variableName;
    }

    @Override
    public Variable getVariable() {
      CsvIndexEntry indexEntry = variableNameIndex.get(variableName);
      if(indexEntry != null) {

        try {
          FileInputStream fis;
          InputStreamReader fr = new InputStreamReader(fis = new FileInputStream(variableFile), CsvDatasource.UTF8);
          CSVReader csvReader = getCsvDatasource().getCsvReader(fr);
          fis.skip(indexEntry.getStart());
          String[] line = csvReader.readNext();
          log.debug("variable line read> {} ", writeArray(line));
          return variableConverter.unmarshal(line);
        } catch(IOException e) {
          throw new MagmaRuntimeException(e);
        }
      }
      throw new MagmaRuntimeException("An index entry does not exist for the variable '" + variableName + "'.");
    }

    private String writeArray(String[] line) {
      StringBuilder sb = new StringBuilder();
      for(String string : line) {
        sb.append(string).append(" ");
      }
      return sb.toString();
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      return ((CsvValueSet) valueSet).getValue(getVariable());
    }

    @Override
    public ValueType getValueType() {
      return getVariable().getValueType();
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
    long result = raf.length();
    raf.close();
    return result;
  }

  public long getDataLastByte() throws IOException {
    return getLastByte(dataFile);
  }

  public long getVariablesLastByte() throws IOException {
    return getLastByte(variableFile);
  }

  private char getLastCharacter(File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    raf.seek(raf.length() - 1);
    return (char) raf.read();
  }

  public char getDataLastCharacter() throws IOException {
    return getLastCharacter(dataFile);
  }

  public char getVariableLastCharacter() throws IOException {
    return getLastCharacter(variableFile);
  }

  private void addNewline(File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "rws");
    raf.seek(raf.length());
    raf.writeChar(NEWLINE_CHARACTER);
    raf.close();
  }

  public void addDataNewline() throws IOException {
    addNewline(dataFile);
    isLastDataCharacterNewline = true;
  }

  public void addVariablesNewline() throws IOException {
    addNewline(variableFile);
    isLastVariablesCharacterNewline = true;
  }

  public void updateDataIndex(VariableEntity entity, long lastByte, String[] line) {
    entityIndex.put(entity, new CsvIndexEntry(lastByte, lastByte + lineLength(line)));
  }

  public void updateVariableIndex(Variable variable, long lastByte, String[] line) {
    variableNameIndex.put(variable.getName(), new CsvIndexEntry(lastByte, lastByte + lineLength(line)));
    if(!hasVariable(variable.getName())) {
      addVariableValueSource(new CsvIndexVariableValueSource(variable.getName()));
    }
  }

  private int lineLength(String[] line) {
    try {
      int length = 0;
      for(String word : line) {
        if(word != null) {
          length += word.getBytes(CsvDatasource.UTF8).length + 2; // word + quote marks
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

  public void setVariablesHeader(String[] header) {
    this.variableConverter = new VariableConverter(header);
  }

  public VariableConverter getVariableConverter() {
    return this.variableConverter;
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
  public Timestamps getTimestamps(ValueSet valueSet) {
    return new CsvTimestamps(variableFile, dataFile);
  }

  /**
   * Convenience method equivalent to {@code (CsvDatasource) getDatasource()}.
   */
  private CsvDatasource getCsvDatasource() {
    return (CsvDatasource) getDatasource();
  }

}

package org.obiba.magma.datasource.csv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
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
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
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

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(CsvValueTable.class);

  private ValueTable refTable;

  private File variableFile;

  private File dataFile;

  private CSVReader variableReader;

  private CSVReader dataReader;

  private CSVVariableEntityProvider variableEntityProvider;

  private String entityType;

  private Map<VariableEntity, ValueSet> entityValueSet = new HashMap<VariableEntity, ValueSet>();

  private LinkedHashMap<VariableEntity, CsvIndexEntry> entityIndex = new LinkedHashMap<VariableEntity, CsvIndexEntry>();

  private boolean isLastDataCharacterNewline;

  private boolean isLastVariablesCharacterNewline;

  @SuppressWarnings("unused")
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
    // CsvIndexEntry indexEntry = entityIndex.get(entity);
    // if(indexEntry != null) {
    // try {
    // FileReader fr = new FileReader(dataFile);
    // CSVReader csvReader = new CSVReader(fr);
    // fr.skip(indexEntry.getStart());
    // return new CsvValueSet(this, entity, dataHeaderMap, csvReader.readNext());
    // } catch(IOException e) {
    // throw new MagmaRuntimeException(e);
    // }
    // } else {
    // throw new NoSuchValueSetException(this, entity);
    // }
    return entityValueSet.get(entity);
  }

  @Override
  public void initialise() {
    try {
      initialiseVariables();
      initialiseData();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void dispose() {
    try {
      if(dataReader != null) dataReader.close();
      if(variableReader != null) variableReader.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void initialiseVariables() throws IOException {
    if(refTable != null) {
      entityType = refTable.getEntityType();
      for(Variable var : refTable.getVariables()) {
        addVariableValueSource(new CsvVariableValueSource(var));
      }
    } else if(variableFile != null) {
      variableReader = new CSVReader(new FileReader(variableFile));

      String[] line = variableReader.readNext();
      // first line is headers
      VariableConverter varConverter = new VariableConverter(line);

      line = variableReader.readNext();
      while(line != null) {
        Variable var = varConverter.unmarshal(line);
        if(entityType == null) entityType = var.getEntityType();
        addVariableValueSource(new CsvVariableValueSource(var));
        line = variableReader.readNext();
      }
    } else {
      entityType = DEFAULT_ENTITY_TYPE;
      if(dataFile != null) {
        // Obtain the variable names from the first line of the data file. Header line is = entity_id + variable names
        CSVReader dataHeaderReader = new CSVReader(new FileReader(dataFile));

        String[] line = dataHeaderReader.readNext();
        if(line != null) {
          for(int i = 1; i < line.length; i++) {
            String variableName = line[i].trim();
            Variable.Builder variableBuilder = Variable.Builder.newVariable(variableName, TextType.get(), entityType);
            addVariableValueSource(new CsvVariableValueSource(variableBuilder.build()));
          }
        }
      }
    }
  }

  CSVWriter getVariableWriter() {
    try {
      return new CSVWriter(new FileWriter(variableFile));
    } catch(IOException e) {
      throw new MagmaRuntimeException("Can not get writer for variable metadata. " + e);
    }
  }

  CSVWriter getValueWriter() {
    try {
      return new CSVWriter(new FileWriter(dataFile, true));
    } catch(IOException e) {
      throw new MagmaRuntimeException("Can not get writer for data. " + e);
    }
  }

  private void initialiseData() throws IOException {
    if(dataFile != null) {
      Map<Integer, CsvIndexEntry> lineIndex = buildLineIndex(dataFile);

      dataReader = new CSVReader(new FileReader(dataFile));

      String[] line = dataReader.readNext();
      // first line is headers = entity_id + variable names
      Map<String, Integer> headerMap = new HashMap<String, Integer>();
      if(line != null) {
        for(int i = 1; i < line.length; i++) {
          headerMap.put(line[i].trim(), i);
        }
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

        ValueSet valueSet = new CsvValueSet(this, entity, headerMap, line);
        entityValueSet.put(entity, valueSet);
        line = dataReader.readNext();
      }

      variableEntityProvider = new CSVVariableEntityProvider(entityType);
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
      if(b == '\n') {
        end = counter - 1;
        lineNumberMap.put(lineCounter, new CsvIndexEntry(start, end));
        start = end + 1;
        // break;
        lineCounter++;
      }
      previousCharacter = b;
    }
    if(previousCharacter != '\n') {
      lineNumberMap.put(lineCounter, new CsvIndexEntry(start, counter));
      isLastDataCharacterNewline = false;
    }
    return lineNumberMap;
  }

  public void clearEntity(VariableEntity entity) throws IOException {
    CsvIndexEntry indexEntry = entityIndex.get(entity);
    if(indexEntry != null) {
      RandomAccessFile raf = new RandomAccessFile(dataFile, "rws");
      int length = (int) (indexEntry.getEnd() - indexEntry.getStart());
      byte[] fill = new byte[length];
      Arrays.fill(fill, "X".getBytes("ISO-8859-1")[0]);
      raf.seek(indexEntry.getStart());
      raf.write(fill);
      entityIndex.remove(entity);
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
      return entityValueSet.keySet();
      // return entityIndex.keySet();
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
    RandomAccessFile raf = new RandomAccessFile(file, "rws");
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

  private void addNewline(File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "rws");
    raf.seek(raf.length());
    raf.writeChar('\n');
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
    entityIndex.put(entity, new CsvIndexEntry(lastByte + 1, lastByte + line.length));
  }
}

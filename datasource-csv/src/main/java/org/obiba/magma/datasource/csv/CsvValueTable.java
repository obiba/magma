package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.csv.converter.VariableConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

public class CsvValueTable extends AbstractValueTable implements Initialisable, Disposable {

  private static final Logger log = LoggerFactory.getLogger(CsvValueTable.class);

  private ValueTable refTable;

  private File variableFile;

  private File dataFile;

  private CSVReader variableReader;

  private CSVReader dataReader;

  private CSVVariableEntityProvider variableEntityProvider;

  private String entityType;

  private Map<VariableEntity, ValueSet> entityValueSet = new HashMap<VariableEntity, ValueSet>();

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
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
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
    }
  }

  private void initialiseData() throws IOException {
    if(dataFile != null) {
      dataReader = new CSVReader(new FileReader(dataFile));

      String[] line = dataReader.readNext();
      // first line is headers = entity_id + variable names
      Map<String, Integer> headerMap = new HashMap<String, Integer>();
      for(int i = 1; i < line.length; i++) {
        headerMap.put(line[i].trim(), i);
      }

      line = dataReader.readNext();
      while(line != null) {
        VariableEntity entity = new VariableEntityBean(entityType, line[0]);
        ValueSet valueSet = new CsvValueSet(this, entity, headerMap, line);
        entityValueSet.put(entity, valueSet);
        line = dataReader.readNext();
      }

      variableEntityProvider = new CSVVariableEntityProvider(entityType);
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
    }

    @Override
    public boolean isForEntityType(String entityType) {
      return getEntityType().equals(entityType);
    }

  }
}

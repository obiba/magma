package org.obiba.magma.datasource.csv;

import java.util.HashMap;
import java.util.Map;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

/**
 * Use this class to build up a line of csv file to be written to a csv file.
 */
public class CsvLine {

  public final static String ENTITY_ID_NAME = "entity_id";

  private Map<String, Integer> headerMap;

  private Map<String, Value> valueMap;

  private int index = 1;

  private final VariableEntity entity;

  public CsvLine(VariableEntity entity) {
    this.entity = entity;
    headerMap = new HashMap<String, Integer>();
    valueMap = new HashMap<String, Value>();
  }

  public void setValue(Variable variable, Value value) {
    if(!headerMap.containsKey(variable.getName())) {
      headerMap.put(variable.getName(), index++);
    }
    valueMap.put(variable.getName(), value);
  }

  public String[] getHeader() {
    String[] line = new String[headerMap.size() + 1];
    line[0] = ENTITY_ID_NAME;
    for(Map.Entry<String, Integer> entry : headerMap.entrySet()) {
      line[entry.getValue()] = entry.getKey();
    }
    return line;
  }

  public String[] getLine() {
    String[] line = new String[headerMap.size() + 1];
    line[0] = entity.getIdentifier();
    for(Map.Entry<String, Integer> entry : headerMap.entrySet()) {
      String value = null;
      if(valueMap.containsKey(entry.getKey())) value = (valueMap.get(entry.getKey())).toString();
      line[entry.getValue()] = value;
    }
    return line;
  }

  public Map<String, Integer> getHeaderMap() {
    return headerMap;
  }

  public void setHeaderMap(Map<String, Integer> headerMap) {
    this.headerMap = headerMap;
  }
}

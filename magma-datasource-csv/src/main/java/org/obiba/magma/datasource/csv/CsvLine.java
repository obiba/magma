package org.obiba.magma.datasource.csv;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.BinaryValueFileHelper;
import org.obiba.magma.type.BinaryType;

/**
 * Use this class to build up a line of csv file to be written to a csv file.
 */
public class CsvLine {

  public final static String ENTITY_ID_NAME = "entity_id";

  private Map<String, Integer> headerMap;

  private final Map<String, Value> valueMap;

  private int index = 1;

  @NotNull
  private final VariableEntity entity;

  @NotNull
  private final File parent;

  public CsvLine(@NotNull VariableEntity entity, @NotNull File parent) {
    this.entity = entity;
    this.parent = parent;
    if(!parent.exists() && !parent.mkdirs()) {
      throw new MagmaRuntimeException("Impossible to create " + parent.getPath() + " directory");
    }
    headerMap = new HashMap<>();
    valueMap = new HashMap<>();
  }

  public void setValue(Variable variable, Value value) {
    if(!headerMap.containsKey(variable.getName())) {
      headerMap.put(variable.getName(), index++);
    }

    Value valueToWrite = variable.getValueType().equals(BinaryType.get()) //
        ? BinaryValueFileHelper.writeValue(parent, variable, entity, value) //
        : value;
    valueMap.put(variable.getName(), valueToWrite);
  }

  @SuppressWarnings("UnusedDeclaration")
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
      String strValue = null;
      String variableName = entry.getKey();
      if(valueMap.containsKey(variableName)) {
        Value value = valueMap.get(variableName);
        strValue = value.toString();
      }
      line[entry.getValue()] = strValue;
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

package org.obiba.magma.datasource.csv;

import java.util.Arrays;
import java.util.Map;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;

public class CsvValueSet extends ValueSetBean {

  private final Map<String, Integer> headerMap;

  private final String[] line;

  public CsvValueSet(ValueTable table, VariableEntity entity, Map<String, Integer> headerMap, String[] line) {
    super(table, entity);
    this.headerMap = headerMap;
    this.line = Arrays.copyOf(line, line.length);
  }

  public Value getValue(Variable variable) {
    Value value = variable.getValueType().nullValue();
    Integer pos = headerMap.get(variable.getName());
    if(pos != null && pos < line.length) {
      String strValue = line[pos];
      if(strValue.length() > 0) {
        value = variable.getValueType().valueOf(strValue);
      }
    }
    return value;
  }

  Map<String, Integer> getHeaderMap() {
    return headerMap;
  }

}

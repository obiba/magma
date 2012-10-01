package org.obiba.magma.datasource.csv;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.obiba.magma.Value;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.BinaryValueStreamLoaderFactory;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.type.BinaryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvValueSet extends ValueSetBean {

  private static final Logger log = LoggerFactory.getLogger(CsvValueSet.class);

  private final Map<String, Integer> headerMap;

  private final String[] line;

  public CsvValueSet(CsvValueTable table, VariableEntity entity, Map<String, Integer> headerMap, String[] line) {
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
        value = getValue(variable, strValue);
      }
    }
    return value;
  }

  private Value getValue(Variable variable, String strValue) {
    Value value;

    if(variable.getValueType().equals(BinaryType.get())) {
      value = getBinaryValue(variable, strValue);
    } else {
      value = getAnyTypeValue(variable, strValue);
    }

    return value;
  }

  private Value getAnyTypeValue(Variable variable, String strValue) {
    if(variable.isRepeatable()) {
      return variable.getValueType().sequenceOf(strValue);
    } else {
      return variable.getValueType().valueOf(strValue);
    }
  }

  private Value getBinaryValue(Variable variable, String strValue) {
    ValueLoaderFactory factory = new BinaryValueStreamLoaderFactory(getParentFile());
    if(variable.isRepeatable()) {
      return BinaryType.get().sequenceOfReferences(factory, strValue);
    } else {
      return BinaryType.get().valueOfReference(factory, strValue);
    }
  }

  private File getParentFile() {
    return ((CsvValueTable) getValueTable()).getParentFile();
  }

  Map<String, Integer> getHeaderMap() {
    return headerMap;
  }

}

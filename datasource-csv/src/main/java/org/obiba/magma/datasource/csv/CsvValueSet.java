package org.obiba.magma.datasource.csv;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.obiba.magma.Value;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.BinaryValueStreamLoaderFactory;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.type.BinaryType;

public class CsvValueSet extends ValueSetBean {

//  private static final Logger log = LoggerFactory.getLogger(CsvValueSet.class);

  private final Map<String, Integer> headerMap;

  private final String[] line;

  public CsvValueSet(ValueTable table, VariableEntity entity, Map<String, Integer> headerMap, String... line) {
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
    return variable.getValueType().equals(BinaryType.get()) //
        ? getBinaryValue(variable, strValue) //
        : getAnyTypeValue(variable, strValue);
  }

  private Value getAnyTypeValue(Variable variable, String strValue) {
    return variable.isRepeatable() //
        ? variable.getValueType().sequenceOf(strValue) //
        : variable.getValueType().valueOf(strValue);
  }

  private Value getBinaryValue(Variable variable, String strValue) {
    ValueLoaderFactory factory = new BinaryValueStreamLoaderFactory(getParentFile());
    return variable.isRepeatable() //
        ? BinaryType.get().sequenceOfReferences(factory, strValue) //
        : BinaryType.get().valueOfReference(factory, strValue);
  }

  private File getParentFile() {
    return ((CsvValueTable) getValueTable()).getParentFile();
  }

}

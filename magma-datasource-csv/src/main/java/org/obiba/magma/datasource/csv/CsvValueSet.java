package org.obiba.magma.datasource.csv;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueLoaderFactory;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.BinaryValueStreamLoaderFactory;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.type.BinaryType;

import au.com.bytecode.opencsv.CSVReader;

public class CsvValueSet extends ValueSetBean {

//  private static final Logger log = LoggerFactory.getLogger(CsvValueSet.class);

  private final Map<String, Integer> headerMap;

  private String[] line;

  private final long skip;

  //
  public CsvValueSet(CsvValueTable table, VariableEntity entity, Map<String, Integer> headerMap, long skip) {
    super(table, entity);
    this.headerMap = headerMap;
    this.skip = skip;
  }

  public Value getValue(Variable variable) {
    Value value = variable.getValueType().nullValue();
    Integer pos = headerMap.get(variable.getName());
    initLine();
    if(pos != null && pos < line.length) {
      String strValue = line[pos];
      if(strValue.length() > 0) {
        try {
          value = getValue(variable, strValue);
        } catch(MagmaRuntimeException e) {
          throw new DatasourceParsingException(
              "Unable to get value for entity " + getVariableEntity().getIdentifier() + " and variable " +
                  variable.getName() + ": " + e.getMessage(), e, "CsvUnableToGetVariableValueForEntity",
              getVariableEntity().getIdentifier(), variable.getName(), e.getMessage());
        }
      }
    }
    return value;
  }

  private void initLine() {
    if (line == null) {
      CsvValueTable csvValueTable = (CsvValueTable)getValueTable();
      try(Reader reader = csvValueTable.getDataReader()) {
        CSVReader csvReader = csvValueTable.getCsvReader(reader);
        csvValueTable.skipSafely(reader, skip);
        String[] csvLine = csvReader.readNext();
        line = Arrays.copyOf(csvLine, csvLine.length);
      } catch(IOException e) {
        throw new MagmaRuntimeException(e);
      }
    }
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

  @Nullable
  private File getParentFile() {
    return ((CsvValueTable) getValueTable()).getParentFile();
  }

}

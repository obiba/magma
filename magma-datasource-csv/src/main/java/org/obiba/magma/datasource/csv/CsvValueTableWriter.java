package org.obiba.magma.datasource.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.csv.converter.VariableConverter;
import org.obiba.magma.support.DatasourceParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvValueTableWriter implements ValueTableWriter {

  private static final Logger log = LoggerFactory.getLogger(CsvValueSet.class);

  private final CsvValueTable valueTable;

  public CsvValueTableWriter(CsvValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Nonnull
  @Override
  public ValueSetWriter writeValueSet(@Nonnull VariableEntity entity) {
    return new CsvValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    return new CsvVariableWriter();
  }

  @Override
  public void close() throws IOException {
  }

  private class CsvVariableWriter implements VariableWriter {

    @Override
    public void writeVariable(@Nonnull Variable variable) {
      try {

        VariableConverter variableConverter = valueTable.getVariableConverter();
        if(valueTable.isVariablesFileEmpty()) {
          // Write Header
          writeVariableToCsv(variableConverter.getHeader());
          valueTable.setVariablesFileEmpty(false);
        } else if(valueTable.hasVariable(variable.getName())) {
          // doing an update.
          valueTable.clearVariable(variable);
        }

        String[] line = variableConverter.marshal(variable);
        long lastByte = valueTable.getVariablesLastByte();
        writeVariableToCsv(line);
        valueTable.updateVariableIndex(variable, lastByte, line);
      } catch(IOException e) {
        throw new MagmaRuntimeException(e);
      }
    }

    private void writeVariableToCsv(String... strings) throws IOException {
      CSVWriter writer = valueTable.getVariableWriter();
      if(writer == null) {
        throw new DatasourceParsingException(
            "Cannot create variable writer. Table " + valueTable.getName() + " does not have variable file.",
            "CsvCannotCreateWriter", valueTable.getName());
      }
      log.trace("write '{}'", Arrays.toString(strings));
      writer.writeNext(strings);
      writer.close();
    }

    @Override
    public void close() throws IOException {
      // Not used.
    }
  }

  private class CsvValueSetWriter implements ValueSetWriter {

    @Nonnull
    private final VariableEntity entity;

    private final CsvLine csvLine;

    private CsvValueSetWriter(@Nonnull VariableEntity entity) {
      this.entity = entity;
      if(valueTable.getParentFile() == null) {
        throw new IllegalArgumentException("valueTable.getParentFile() cannot be null");
      }
      //noinspection ConstantConditions
      csvLine = new CsvLine(entity, valueTable.getParentFile());

      // Populate with existing values, if available
      if(valueTable.hasValueSet(entity)) {
        ValueSet valueSet = valueTable.getValueSet(entity);
        for(Variable variable : valueTable.getVariables()) {
          writeValue(variable, valueTable.getValue(variable, valueSet));
        }
      }
    }

    @Override
    public void writeValue(@Nonnull Variable variable, Value value) {
      csvLine.setValue(variable, value);
    }

    @Override
    public void close() throws IOException {

      if(valueTable.isDataFileEmpty()) {
        writeTableWithoutData();
      } else {
        // Test header is a subset
        List<String> extraHeaders = getExtraHeadersFromNewValueSet(getExistingHeaderMap(), csvLine.getHeaderMap());
        if(extraHeaders.size() != 0) {
          StringBuilder sb = new StringBuilder();
          for(String header : extraHeaders) {
            sb.append(header).append(" ");
          }
          throw new MagmaRuntimeException("Cannot update the CSV ValueTable [" + valueTable.getName() +
              "]. The new ValueSet (record) included the following unexpected Variables (fields): " + sb.toString());
        }

        if(valueTable.hasValueSet(entity)) {
          // Delete existing value set.
          valueTable.clearEntity(entity);
        }
        // Set existing header
        csvLine.setHeaderMap(getExistingHeaderMap());
      }

      // Writer Value set. Throw exception if doesn't match header
      long lastByte = valueTable.getDataLastByte();
      String[] line = csvLine.getLine();
      writeValueToCsv(line);
      // Update index
      valueTable.updateDataIndex(entity, lastByte, line);
    }

    private void writeTableWithoutData() throws IOException {
      // Write Header
      if(valueTable.getDataHeaderMap().isEmpty()) {
        Map<String, Integer> generatedHeader = generateDataHeaderMapFromVariables();
        if(generatedHeader.size() > 0) {
          valueTable.setDataHeaderMap(generateDataHeaderMapFromVariables());
          csvLine.setHeaderMap(valueTable.getDataHeaderMap());
        } else {
          valueTable.setDataHeaderMap(csvLine.getHeaderMap());
        }
      }

      writeValueToCsv(valueTable.getDataHeaderAsArray());
      getExistingHeaderMap();
      valueTable.setDataHeaderMap(csvLine.getHeaderMap());
      valueTable.setDataFileEmpty(false);
    }

    private void writeValueToCsv(String... strings) throws IOException {
      CSVWriter writer = valueTable.getValueWriter();
      if(writer == null) {
        throw new DatasourceParsingException(
            "Cannot create data writer. Table " + valueTable.getName() + " does not have data file.",
            "CsvCannotCreateWriter", valueTable.getName());
      }
      log.trace("write '{}'", Arrays.toString(strings));
      writer.writeNext(strings);
      writer.close();
    }

    private Map<String, Integer> getExistingHeaderMap() {
      return valueTable.getDataHeaderMap();
    }

    private List<String> getExtraHeadersFromNewValueSet(Map<String, Integer> existingHeaderMap,
        Map<String, Integer> valueSetHeaderMap) {
      List<String> result = new ArrayList<String>();
      for(Map.Entry<String, Integer> entry : valueSetHeaderMap.entrySet()) {
        if(!existingHeaderMap.containsKey(entry.getKey())) {
          result.add(entry.getKey());
        }
      }
      return result;
    }

    public Map<String, Integer> generateDataHeaderMapFromVariables() {
      Map<String, Integer> headerMap = new HashMap<String, Integer>();
      int count = 1;
      for(Variable variable : valueTable.getVariables()) {
        headerMap.put(variable.getName(), count++);
      }
      return headerMap;
    }

  }

}

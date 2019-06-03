/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.csv;

import au.com.bytecode.opencsv.CSVWriter;
import org.obiba.magma.*;
import org.obiba.magma.datasource.csv.converter.VariableConverter;
import org.obiba.magma.support.DatasourceParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;

public class CsvValueTableWriter implements ValueTableWriter {

  private static final Logger log = LoggerFactory.getLogger(CsvValueSet.class);

  private final CsvValueTable valueTable;

  private final CSVWriter csvValueWriter;

  public CsvValueTableWriter(CsvValueTable valueTable) {
    this.valueTable = valueTable;
    this.csvValueWriter = valueTable.getValueWriter();
  }

  @NotNull
  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    return new CsvValueSetWriter(entity);
  }

  @Override
  public VariableWriter writeVariables() {
    return new CsvVariableWriter();
  }

  @Override
  public void close() {
    if (csvValueWriter != null) {
      try {
        csvValueWriter.close();
      } catch (IOException e) {
        log.error("Error while closing CSV writer", e);
      }
    }
  }

  private class CsvVariableWriter implements VariableWriter {

    @Override
    public void writeVariable(@NotNull Variable variable) {
      try {

        VariableConverter variableConverter = valueTable.getVariableConverter();
        if (valueTable.isVariablesFileEmpty()) {
          // Write Header
          writeVariableToCsv(variableConverter.getHeader());
          valueTable.setVariablesFileEmpty(false);
        }

        String[] line = variableConverter.marshal(variable);
        writeVariableToCsv(line);
      } catch (IOException e) {
        throw new MagmaRuntimeException(e);
      }
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      throw new UnsupportedOperationException("Variable cannot be removed from a CSV file");
    }

    private void writeVariableToCsv(String... strings) throws IOException {
      try (CSVWriter writer = valueTable.getVariableWriter()) {
        if (writer == null) {
          throw new DatasourceParsingException(
              "Cannot create variable writer. Table " + valueTable.getName() + " does not have variable file.",
              "CsvCannotCreateWriter", valueTable.getName());
        }
        log.trace("write '{}'", Arrays.toString(strings));
        writer.writeNext(strings);
      }
    }

    @Override
    public void close() {
    }

  }

  private class CsvValueSetWriter implements ValueSetWriter {

    @NotNull
    private final VariableEntity entity;

    private final CsvLine csvLine;

    private CsvValueSetWriter(@NotNull VariableEntity entity) {
      this.entity = entity;
      //noinspection ConstantConditions
      csvLine = new CsvLine(entity, valueTable.getParentFile(), valueTable.isMultilines());

      // Populate with existing values, if available
      if (valueTable.hasValueSet(entity)) {
        ValueSet valueSet = valueTable.getValueSet(entity);
        for (Variable variable : valueTable.getVariables()) {
          writeValue(variable, valueTable.getValue(variable, valueSet));
        }
      }
    }

    @Override
    public void writeValue(@NotNull Variable variable, Value value) {
      csvLine.setValue(variable, value);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() {

      try {
        if (valueTable.isDataFileEmpty()) {
          writeTableWithoutData();
        } else {
          writeTableWithData();
        }

        // Writer Value set. Throw exception if doesn't match header
        csvLine.getLines().forEach(line -> {
          try {
            writeValueToCsv(line);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
        // Update entities index
        valueTable.getVariableEntityProvider().add(entity);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private void writeTableWithoutData() throws IOException {
      // Write Header
      if (valueTable.getDataHeaderMap().isEmpty()) {
        Map<String, Integer> generatedHeader = generateDataHeaderMapFromVariables();
        if (generatedHeader.size() > 0) {
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

    private void writeTableWithData() throws IOException {// Test header is a subset
      List<String> extraHeaders = getExtraHeadersFromNewValueSet(getExistingHeaderMap(), csvLine.getHeaderMap());
      if (extraHeaders.size() != 0) {
        StringBuilder sb = new StringBuilder();
        for (String header : extraHeaders) {
          sb.append(header).append(" ");
        }
        throw new MagmaRuntimeException("Cannot update the CSV ValueTable [" + valueTable.getName() +
            "]. The new ValueSet (record) included the following unexpected Variables (fields): " + sb.toString());
      }

      if (valueTable.hasValueSet(entity)) {
        // Delete existing value set.
        //valueTable.clearEntity(entity);
      }
      // Set existing header
      csvLine.setHeaderMap(getExistingHeaderMap());
    }

    private void writeValueToCsv(String... strings) throws IOException {
      if (csvValueWriter == null) {
        throw new DatasourceParsingException(
            "Cannot create data writer. Table " + valueTable.getName() + " does not have data file.",
            "CsvCannotCreateWriter", valueTable.getName());
      }
      log.trace("write '{}'", Arrays.toString(strings));
      csvValueWriter.writeNext(strings);
    }

    private Map<String, Integer> getExistingHeaderMap() {
      return valueTable.getDataHeaderMap();
    }

    private List<String> getExtraHeadersFromNewValueSet(Map<String, Integer> existingHeaderMap,
                                                        Map<String, Integer> valueSetHeaderMap) {
      List<String> result = new ArrayList<>();
      for (Map.Entry<String, Integer> entry : valueSetHeaderMap.entrySet()) {
        if (!existingHeaderMap.containsKey(entry.getKey())) {
          result.add(entry.getKey());
        }
      }
      return result;
    }

    public Map<String, Integer> generateDataHeaderMapFromVariables() {
      Map<String, Integer> headerMap = new HashMap<>();
      int count = 1;
      for (Variable variable : valueTable.getVariables()) {
        headerMap.put(variable.getName(), count++);
      }
      return headerMap;
    }

  }

}

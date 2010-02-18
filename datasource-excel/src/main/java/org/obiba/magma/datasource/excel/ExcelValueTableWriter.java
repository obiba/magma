package org.obiba.magma.datasource.excel;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.Attribute;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

public class ExcelValueTableWriter implements ValueTableWriter {

  private ExcelValueTable valueTable;

  public ExcelValueTableWriter(ExcelValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public VariableWriter writeVariables() {
    return new ExcelVariableWriter(valueTable);
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }

  private class ExcelVariableWriter implements VariableWriter {

    private ExcelDatasource excelDatasource;

    private ValueTable valueTable;

    private Sheet variablesSheet;

    public ExcelVariableWriter(ValueTable valueTable) {
      this.valueTable = valueTable;
      excelDatasource = (ExcelDatasource) valueTable.getDatasource();
      this.variablesSheet = ((ExcelDatasource) valueTable.getDatasource()).getVariablesSheet();
    }

    public void writeVariable(Variable variable) {
      Row headerRow = variablesSheet.getRow(0);
      if(headerRow == null) {
        headerRow = variablesSheet.createRow(0);
      }

      Map<String, Integer> headerMap = ExcelDatasource.mapHeader(headerRow);

      Cell headerCell;
      for(String reservedAttributeName : ExcelDatasource.variablesReservedAttributeNames) {
        if(headerMap.get(reservedAttributeName) == null) {
          headerMap.put(reservedAttributeName, Integer.valueOf(getLastCellNum(headerRow)));
          headerCell = headerRow.createCell(getLastCellNum(headerRow));
          headerCell.setCellValue(reservedAttributeName);
          headerCell.setCellStyle(excelDatasource.getExcelStyle("headerCellStyle"));
        }
      }

      Row attributesRow = variablesSheet.createRow(variablesSheet.getPhysicalNumberOfRows());

      attributesRow.createCell(headerMap.get("table")).setCellValue(valueTable.getName());
      attributesRow.createCell(headerMap.get("name")).setCellValue(variable.getName());
      // attributesRow.createCell(headerMap.get("mimeType")).setCellValue(variable.getMimeType());
      // attributesRow.createCell(headerMap.get("occurenceGroup")).setCellValue(variable.getOccurrenceGroup());
      attributesRow.createCell(headerMap.get("entityType")).setCellValue(variable.getEntityType());
      // attributesRow.createCell(headerMap.get("unit")).setCellValue(variable.getUnit());
      // attributesRow.createCell(headerMap.get("repeatable")).setCellValue(String.valueOf(variable.isRepeatable()));
      attributesRow.createCell(headerMap.get("valueType")).setCellValue(String.valueOf(variable.getValueType().getName()));

      String customAttributeName;
      Locale customAttributeLocale;
      StringBuilder stringBuilder = new StringBuilder();
      Integer attributeCellIndex;
      for(Attribute customAttribute : variable.getAttributes()) {
        customAttributeLocale = customAttribute.getLocale();
        customAttributeName = stringBuilder.append(customAttribute.getName()).append(customAttributeLocale != null ? customAttribute.getName() + ":" + customAttributeLocale : "").toString();
        attributeCellIndex = headerMap.get(customAttributeName);
        if(attributeCellIndex == null) {
          headerMap.put(customAttributeName, Integer.valueOf(getLastCellNum(headerRow)));
          attributeCellIndex = Integer.valueOf(getLastCellNum(headerRow));
          headerCell = headerRow.createCell(attributeCellIndex);
          headerCell.setCellValue(customAttributeName);
          headerCell.setCellStyle(excelDatasource.getExcelStyle("headerCellStyle"));
        }
        attributesRow.createCell(attributeCellIndex).setCellValue(customAttribute.getValue().getValue().toString());
        stringBuilder.setLength(0);
      }

    }

    private int getLastCellNum(Row row) {
      return row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
    }

    @Override
    public void close() throws IOException {
      // TODO Auto-generated method stub

    }

  }

  private class ExcelValueSetWriter implements ValueSetWriter {

    @Override
    public void writeValue(Variable variable, Value value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void close() throws IOException {
      // TODO Auto-generated method stub
    }

  }

}

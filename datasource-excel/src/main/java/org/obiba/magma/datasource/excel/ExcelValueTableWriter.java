package org.obiba.magma.datasource.excel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.Category;
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

    private Sheet categoriesSheet;

    public ExcelVariableWriter(ValueTable valueTable) {
      this.valueTable = valueTable;
      excelDatasource = (ExcelDatasource) valueTable.getDatasource();
      this.variablesSheet = ((ExcelDatasource) valueTable.getDatasource()).getVariablesSheet();
      this.categoriesSheet = ((ExcelDatasource) valueTable.getDatasource()).getCategoriesSheet();
    }

    public void writeVariable(Variable variable) {

      Row headerRowVariables = variablesSheet.getRow(0);
      if(headerRowVariables == null) {
        headerRowVariables = variablesSheet.createRow(0);
      }

      Map<String, Integer> headerMapVariables = ExcelDatasource.mapHeader(headerRowVariables);
      updateHeaderRow(headerMapVariables, headerRowVariables, ExcelDatasource.variablesReservedAttributeNames);

      Row attributesRow = variablesSheet.createRow(variablesSheet.getPhysicalNumberOfRows());
      attributesRow.createCell(headerMapVariables.get("table")).setCellValue(valueTable.getName());
      attributesRow.createCell(headerMapVariables.get("name")).setCellValue(variable.getName());
      // attributesRow.createCell(headerMapVariables.get("mimeType")).setCellValue(variable.getMimeType());
      // attributesRow.createCell(headerMapVariables.get("occurenceGroup")).setCellValue(variable.getOccurrenceGroup());
      attributesRow.createCell(headerMapVariables.get("entityType")).setCellValue(variable.getEntityType());
      // attributesRow.createCell(headerMapVariables.get("unit")).setCellValue(variable.getUnit());
      // attributesRow.createCell(headerMapVariables.get("repeatable")).setCellValue(String.valueOf(variable.isRepeatable()));
      attributesRow.createCell(headerMapVariables.get("valueType")).setCellValue(String.valueOf(variable.getValueType().getName()));

      addCustomAttributes(variable, headerRowVariables, headerMapVariables, attributesRow);

      Row headerRowCategories = categoriesSheet.getRow(0);
      if(headerRowCategories == null) {
        headerRowCategories = categoriesSheet.createRow(0);
      }

      Map<String, Integer> headerMapCategories = ExcelDatasource.mapHeader(headerRowCategories);
      updateHeaderRow(headerMapCategories, headerRowCategories, ExcelDatasource.categoriesReservedAttributeNames);

      Set<Category> categories = variable.getCategories();
      Row categoryRow;
      for(Category category : categories) {
        categoryRow = categoriesSheet.createRow(categoriesSheet.getPhysicalNumberOfRows());
        categoryRow.createCell(headerMapCategories.get("table")).setCellValue(valueTable.getName());
        categoryRow.createCell(headerMapCategories.get("variable")).setCellValue(variable.getName());
        categoryRow.createCell(headerMapCategories.get("name")).setCellValue(category.getName());
        categoryRow.createCell(headerMapCategories.get("code")).setCellValue(category.getCode());
        // categoryRow.createCell(headerMapCategories.get("missing")).setCellValue(category.isMissing());
        addCustomAttributes(category, headerRowCategories, headerMapCategories, attributesRow);
      }

    }

    /**
     * @param variable
     * @param headerRow
     * @param headerMap
     * @param attributesRow
     */
    private void addCustomAttributes(AttributeAware attributeAware, Row headerRow, Map<String, Integer> headerMap, Row attributesRow) {
      String customAttributeName;
      Locale customAttributeLocale;
      StringBuilder stringBuilder = new StringBuilder();
      Integer attributeCellIndex;
      Cell headerCell;
      for(Attribute customAttribute : attributeAware.getAttributes()) {
        customAttributeLocale = customAttribute.getLocale();
        customAttributeName = stringBuilder.append(customAttribute.getName()).append(customAttributeLocale != null ? ":" + customAttributeLocale : "").toString();
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

    private void updateHeaderRow(Map<String, Integer> headerMap, Row headerRow, List<String> columnNames) {

      Cell headerCell;
      for(String reservedAttributeName : columnNames) {
        if(headerMap.get(reservedAttributeName) == null) {
          headerMap.put(reservedAttributeName, Integer.valueOf(getLastCellNum(headerRow)));
          headerCell = headerRow.createCell(getLastCellNum(headerRow));
          headerCell.setCellValue(reservedAttributeName);
          headerCell.setCellStyle(excelDatasource.getExcelStyle("headerCellStyle"));
        }
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

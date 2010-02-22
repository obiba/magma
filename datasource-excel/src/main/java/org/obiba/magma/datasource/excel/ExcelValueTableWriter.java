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
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

public class ExcelValueTableWriter implements ValueTableWriter {

  private ExcelValueTable valueTable;

  private VariableWriter excelVariableWriter;

  public ExcelValueTableWriter(ExcelValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public VariableWriter writeVariables() {
    excelVariableWriter = new ExcelVariableWriter(valueTable);
    return excelVariableWriter;
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws IOException {
    excelVariableWriter.close();
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
      Row attributesRow = writeVariableAttributes(variable);
      writeCategories(variable, attributesRow);

    }

    /**
     * Writes the Categories of a Variable to the "Categories" Excel sheet.
     * 
     * @param variable
     * @param attributesRow
     */
    private void writeCategories(Variable variable, Row attributesRow) {
      Row headerRowCategories = categoriesSheet.getRow(0);
      if(headerRowCategories == null) {
        headerRowCategories = categoriesSheet.createRow(0);
      }

      Map<String, Integer> headerMapCategories = ExcelDatasource.mapSheetHeader(headerRowCategories);
      updateSheetHeaderRow(headerMapCategories, headerRowCategories, ExcelDatasource.categoriesReservedAttributeNames);

      Set<Category> categories = variable.getCategories();
      Row categoryRow;
      for(Category category : categories) {
        categoryRow = categoriesSheet.createRow(categoriesSheet.getPhysicalNumberOfRows());
        ExcelUtil.setCellValue(categoryRow.createCell(headerMapCategories.get("table")), TextType.get(), valueTable.getName());
        ExcelUtil.setCellValue(categoryRow.createCell(headerMapCategories.get("variable")), TextType.get(), variable.getName());
        ExcelUtil.setCellValue(categoryRow.createCell(headerMapCategories.get("name")), TextType.get(), category.getName());
        ExcelUtil.setCellValue(categoryRow.createCell(headerMapCategories.get("code")), TextType.get(), category.getCode());
        ExcelUtil.setCellValue(categoryRow.createCell(headerMapCategories.get("missing")), BooleanType.get(), category.isMissing());

        writeCustomAttributes(category, attributesRow, headerRowCategories, headerMapCategories);
      }
    }

    /**
     * Writes the Attributes of one Variable to a Row in the "Variables" Excel sheet.
     * 
     * @param variable
     * @return The row where attributes were written to.
     */
    private Row writeVariableAttributes(Variable variable) {
      Row headerRowVariables = variablesSheet.getRow(0);
      if(headerRowVariables == null) {
        headerRowVariables = variablesSheet.createRow(0);
      }

      Map<String, Integer> headerMapVariables = ExcelDatasource.mapSheetHeader(headerRowVariables);
      updateSheetHeaderRow(headerMapVariables, headerRowVariables, ExcelDatasource.variablesReservedAttributeNames);

      Row attributesRow = variablesSheet.createRow(variablesSheet.getPhysicalNumberOfRows());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("table")), TextType.get(), valueTable.getName());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("name")), TextType.get(), variable.getName());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("mimeType")), TextType.get(), variable.getMimeType());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("occurrenceGroup")), TextType.get(), variable.getOccurrenceGroup());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("entityType")), TextType.get(), variable.getEntityType());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("unit")), TextType.get(), variable.getUnit());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("repeatable")), BooleanType.get(), variable.isRepeatable());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("valueType")), TextType.get(), variable.getValueType().getName());

      writeCustomAttributes(variable, attributesRow, headerRowVariables, headerMapVariables);
      return attributesRow;
    }

    /**
     * Writes the custom Attributes of an AttributeAware instance (ex: Variable, Category...) to a Row in an Excel
     * sheet.
     * 
     * @param variable
     * @param headerRow
     * @param headerMap
     * @param attributesRow
     */
    private void writeCustomAttributes(AttributeAware attributeAware, Row attributesRow, Row headerRow, Map<String, Integer> headerMap) {
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
          headerCell.setCellStyle(excelDatasource.getExcelStyles("headerCellStyle"));
        }
        ExcelUtil.setCellValue(attributesRow.createCell(attributeCellIndex), customAttribute.getValue());
        stringBuilder.setLength(0);
      }
    }

    /**
     * Update the header Row (usually the first Row) in an Excel sheet to make sure that all provided column names are
     * represented.
     * 
     * @param headerMap
     * @param headerRow
     * @param columnNames
     */
    private void updateSheetHeaderRow(Map<String, Integer> headerMap, Row headerRow, List<String> columnNames) {

      Cell headerCell;
      for(String reservedAttributeName : columnNames) {
        if(headerMap.get(reservedAttributeName) == null) {
          headerMap.put(reservedAttributeName, Integer.valueOf(getLastCellNum(headerRow)));
          headerCell = headerRow.createCell(getLastCellNum(headerRow));
          headerCell.setCellValue(reservedAttributeName);
          headerCell.setCellStyle(excelDatasource.getExcelStyles("headerCellStyle"));
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

}

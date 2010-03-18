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
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

public class ExcelValueTableWriter implements ValueTableWriter {

  private ExcelValueTable valueTable;

  public ExcelValueTableWriter(ExcelValueTable valueTable) {
    this.valueTable = valueTable;
    // First column is for storing the Variable Entity identifiers
    Cell cell = valueTable.getValueTableSheet().getRow(0).createCell(0);
    ExcelUtil.setCellValue(cell, TextType.get(), "Entity ID");
    cell.setCellStyle(valueTable.getDatasource().getExcelStyles("headerCellStyle"));
  }

  @Override
  public VariableWriter writeVariables() {
    return new ExcelVariableWriter();
  }

  @Override
  public ValueSetWriter writeValueSet(VariableEntity entity) {
    return new ExcelValueSetWriter(entity);
  }

  @Override
  public void close() throws IOException {
  }

  private class ExcelVariableWriter implements VariableWriter {

    private Sheet variablesSheet;

    private Sheet categoriesSheet;

    private Sheet attributesSheet;

    public ExcelVariableWriter() {
      this.variablesSheet = valueTable.getDatasource().getVariablesSheet();
      this.categoriesSheet = valueTable.getDatasource().getCategoriesSheet();
      // OPAL-173: Removed the attributesSheet
      // this.attributesSheet = ((ExcelDatasource) valueTable.getDatasource()).getAttributesSheet();

    }

    public void writeVariable(Variable variable) {
      // Will create the column if it doesn't exist.
      valueTable.getVariableColumn(variable);
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
        categoryRow = valueTable.getCategoryRow(variable, category);
        ExcelUtil.setCellValue(categoryRow.getCell(headerMapCategories.get("table"), Row.CREATE_NULL_AS_BLANK), TextType.get(), valueTable.getName());
        ExcelUtil.setCellValue(categoryRow.getCell(headerMapCategories.get("variable"), Row.CREATE_NULL_AS_BLANK), TextType.get(), variable.getName());
        ExcelUtil.setCellValue(categoryRow.getCell(headerMapCategories.get("name"), Row.CREATE_NULL_AS_BLANK), TextType.get(), category.getName());
        ExcelUtil.setCellValue(categoryRow.getCell(headerMapCategories.get("code"), Row.CREATE_NULL_AS_BLANK), TextType.get(), category.getCode());
        ExcelUtil.setCellValue(categoryRow.getCell(headerMapCategories.get("missing"), Row.CREATE_NULL_AS_BLANK), BooleanType.get(), category.isMissing());

        writeCustomAttributes(category, "category", category.getName(), categoryRow, headerRowCategories, headerMapCategories);
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

      // Get the row for this variable in the variables sheet. If it doesn't exist yet, an empty one is created.
      Row variableRow = valueTable.getVariableRow(variable);

      ExcelUtil.setCellValue(variableRow.getCell(headerMapVariables.get("table"), Row.CREATE_NULL_AS_BLANK), TextType.get(), valueTable.getName());
      ExcelUtil.setCellValue(variableRow.getCell(headerMapVariables.get("name"), Row.CREATE_NULL_AS_BLANK), TextType.get(), variable.getName());
      ExcelUtil.setCellValue(variableRow.getCell(headerMapVariables.get("mimeType"), Row.CREATE_NULL_AS_BLANK), TextType.get(), variable.getMimeType());
      ExcelUtil.setCellValue(variableRow.getCell(headerMapVariables.get("occurrenceGroup"), Row.CREATE_NULL_AS_BLANK), TextType.get(), variable.getOccurrenceGroup());
      ExcelUtil.setCellValue(variableRow.getCell(headerMapVariables.get("entityType"), Row.CREATE_NULL_AS_BLANK), TextType.get(), variable.getEntityType());
      ExcelUtil.setCellValue(variableRow.getCell(headerMapVariables.get("unit"), Row.CREATE_NULL_AS_BLANK), TextType.get(), variable.getUnit());
      ExcelUtil.setCellValue(variableRow.getCell(headerMapVariables.get("repeatable"), Row.CREATE_NULL_AS_BLANK), BooleanType.get(), variable.isRepeatable());
      ExcelUtil.setCellValue(variableRow.getCell(headerMapVariables.get("valueType"), Row.CREATE_NULL_AS_BLANK), TextType.get(), variable.getValueType().getName());

      writeCustomAttributes(variable, "variable", variable.getName(), variableRow, headerRowVariables, headerMapVariables);
      return variableRow;
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
    private void writeCustomAttributes(AttributeAware attributeAware, String attributeAwareType, String attributeAwareName, Row attributesRow, Row headerRow, Map<String, Integer> headerMap) {
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
          headerCell.setCellStyle(valueTable.getDatasource().getExcelStyles("headerCellStyle"));
        }
        ExcelUtil.setCellValue(attributesRow.getCell(attributeCellIndex, Row.CREATE_NULL_AS_BLANK), customAttribute.getValue());
        stringBuilder.setLength(0);

        // OPAL-145: Write the attribute type on the "Attributes" sheet.
        // OPAL-173: commented out to support writing large amounts of variables. Writing the attribute sheet results in
        // writing more than 65536 rows most of time.
        // writeCustomAttributeType(attributeAwareType, attributeAwareName, customAttributeName,
        // customAttribute.getValueType());
      }
    }

    private void writeCustomAttributeType(String attributeAwareType, String attributeAwareName, String attributeName, ValueType attributeType) {
      Row headerRow = attributesSheet.getRow(0);
      if(headerRow == null) {
        headerRow = attributesSheet.createRow(0);
      }

      Map<String, Integer> headerMapVariables = ExcelDatasource.mapSheetHeader(headerRow);
      updateSheetHeaderRow(headerMapVariables, headerRow, ExcelDatasource.attributesReservedAttributeNames);

      Row attributesRow = attributesSheet.createRow(attributesSheet.getPhysicalNumberOfRows());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("table")), TextType.get(), valueTable.getName());
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("attributeAwareType")), TextType.get(), attributeAwareType);
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("attributeAware")), TextType.get(), attributeAwareName);
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("attribute")), TextType.get(), attributeName);
      ExcelUtil.setCellValue(attributesRow.createCell(headerMapVariables.get("valueType")), TextType.get(), attributeType.getName());
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
          headerCell.setCellStyle(valueTable.getDatasource().getExcelStyles("headerCellStyle"));
        }
      }
    }

    private int getLastCellNum(Row row) {
      return row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
    }

    @Override
    public void close() throws IOException {
    }

  }

  private class ExcelValueSetWriter implements ValueSetWriter {

    private Row entityRow;

    private ExcelValueSetWriter(VariableEntity entity) {
      Sheet tableSheet = valueTable.getValueTableSheet();
      entityRow = tableSheet.createRow(tableSheet.getPhysicalNumberOfRows());
      ExcelUtil.setCellValue(entityRow.createCell(0), TextType.get(), entity.getIdentifier());
    }

    @Override
    public void writeValue(Variable variable, Value value) {
      int variableColumn = valueTable.getVariableColumn(variable);
      ExcelUtil.setCellValue(entityRow.createCell(variableColumn), value);
    }

    @Override
    public void close() throws IOException {
    }

  }

}

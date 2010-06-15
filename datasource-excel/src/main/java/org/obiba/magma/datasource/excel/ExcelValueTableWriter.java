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
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

public class ExcelValueTableWriter implements ValueTableWriter {

  private ExcelValueTable valueTable;

  public ExcelValueTableWriter(ExcelValueTable valueTable) {
    this.valueTable = valueTable;
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

    public ExcelVariableWriter() {
      this.variablesSheet = valueTable.getDatasource().getVariablesSheet();
      this.categoriesSheet = valueTable.getDatasource().getCategoriesSheet();
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

      Map<String, Integer> headerMapCategories = valueTable.getDatasource().getCategoriesHeaderMap();
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

      Map<String, Integer> headerMapVariables = valueTable.getDatasource().getVariablesHeaderMap();
      updateSheetHeaderRow(headerMapVariables, headerRowVariables, ExcelDatasource.variablesReservedAttributeNames);

      // Get the row for this variable in the variables sheet. If it doesn't exist yet, an empty one is created.
      Row variableRow = getVariableRow(variable);

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
     * Returns the {@code Row} from the variable sheet for the specified variable. If no such row currently exists, a
     * new one is added and returned.
     * @param variable
     * @return
     */
    private Row getVariableRow(Variable variable) {
      Row row = valueTable.getVariableRows().get(variable.getName());
      if(row == null) {
        Sheet variables = valueTable.getDatasource().getVariablesSheet();
        row = variables.createRow(variables.getPhysicalNumberOfRows());
        valueTable.getVariableRows().put(variable.getName(), row);
      }
      return row;
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

    private final Row entityRow;

    private ExcelValueSetWriter(VariableEntity entity) {
      Sheet tableSheet = valueTable.getValueTableSheet();
      entityRow = tableSheet.createRow(tableSheet.getPhysicalNumberOfRows());
      ExcelUtil.setCellValue(entityRow.createCell(0), TextType.get(), entity.getIdentifier());
    }

    @Override
    public void writeValue(Variable variable, Value value) {
      // Will create the column if it doesn't exist.
      int variableColumn = valueTable.getVariableColumn(variable);
      ExcelUtil.setCellValue(entityRow.createCell(variableColumn), value);
    }

    @Override
    public void close() throws IOException {
    }

  }

}

package org.obiba.magma.datasource.excel.support;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.ExcelValueTable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.Maps;

public class VariableConverter {

  private Map<String, Integer> headerMapVariables;

  private Map<String, Integer> headerMapCategories;

  private Set<String> attributeNamesCategories;

  private Set<String> attributeNamesVariables;

  /** Maps a variable's name to its Row in the variablesSheet */
  private final Map<String, Row> variableRows = Maps.newHashMap();

  /** Maps a category's name concatenated with the variable's name to its Row in the variablesSheet */
  private final Map<String, Row> categoryRows = Maps.newHashMap();

  private ExcelValueTable valueTable;

  public VariableConverter(ExcelValueTable valueTable) {
    super();
    this.valueTable = valueTable;
  }

  /**
   * Check if is a variable row for the current table.
   * @param variableRow
   * @return
   */
  public boolean isVariableRow(Row variableRow) {
    String table = getVariableCellValue(variableRow, "table");
    return valueTable.getName().equals(table);
  }

  //
  // unmarshall
  //

  public Variable unmarshall(Row variableRow) {
    String name = getVariableCellValue(variableRow, "name");
    String valueType = getVariableCellValue(variableRow, "valueType");
    String entityType = getVariableCellValue(variableRow, "entityType");
    String mimeType = getVariableCellValue(variableRow, "mimeType");
    String unit = getVariableCellValue(variableRow, "unit");
    String occurrenceGroup = getVariableCellValue(variableRow, "occurrenceGroup");

    Variable.Builder builder = Variable.Builder.newVariable(name, ValueType.Factory.forName(valueType), entityType).mimeType(mimeType).unit(unit).occurrenceGroup(occurrenceGroup);
    Boolean repeatable = getVariableCellValue(variableRow, "repeatable") == "1";

    if(repeatable) {
      builder.repeatable();
    }

    unmarshallCustomAttributes("variable", name, variableRow, getHeaderMapVariables(), getAttributeNamesVariables(), builder);

    unmarshallCategories(name, builder);

    variableRows.put(name, variableRow);

    return builder.build();
  }

  /**
   * Read the Categories of a specific Variable in the "Categories" Excel sheet. Add these Categories to the specified
   * Variable using the Variable.Builder.
   * 
   * @param variableName
   * @param variableBuilder
   */
  private void unmarshallCategories(String variableName, Variable.Builder variableBuilder) {

    String categoryTable;
    String categoryVariable;

    Row categoryRow;

    if(getHeaderMapCategories() == null) return;

    Sheet categoriesSheet = valueTable.getDatasource().getCategoriesSheet();
    int categoryRowCount = categoriesSheet.getPhysicalNumberOfRows();
    for(int x = 1; x < categoryRowCount; x++) {
      categoryRow = categoriesSheet.getRow(x);
      categoryTable = getCategoryCellValue(categoryRow, "table");
      categoryVariable = getCategoryCellValue(categoryRow, "variable");

      if(categoryTable.equals(valueTable.getName()) && categoryVariable.equals(variableName)) {
        Category category = unmarshallCategory(categoryRow);
        variableBuilder.addCategory(category);
        String key = variableName + category.getName();
        categoryRows.put(key, categoryRow);
      }
    }
  }

  private Category unmarshallCategory(Row categoryRow) {
    String categoryName = getCategoryCellValue(categoryRow, "name");
    String categoryCode = getCategoryCellValue(categoryRow, "code");
    boolean missing = parseBoolean(getCategoryCellValue(categoryRow, "missing"));

    AttributeAwareBuilder<Category.Builder> categoryBuilder = Category.Builder.newCategory(categoryName).withCode(categoryCode).missing(missing);
    unmarshallCustomAttributes("category", categoryName, categoryRow, getHeaderMapCategories(), getAttributeNamesCategories(), categoryBuilder);
    return ((Category.Builder) categoryBuilder).build();
  }

  private boolean parseBoolean(String str) {
    return (str.equals("1") || str.equalsIgnoreCase("true") || str.equalsIgnoreCase("no"));
  }

  /**
   * Read the custom Attributes define by a Row in an Excel sheet to an AttributeAware instance (ex: Variable,
   * Category...) using an AttributeAwareBuilder.
   * 
   * @param variableRow
   * @param headerMap
   * @param attributeNames
   * @param variableBuilder
   */
  private void unmarshallCustomAttributes(String attributeAwareType, String attributeAwareName, Row attributesRow, Map<String, Integer> headerMap, Set<String> attributeNames, AttributeAwareBuilder<?> builder) {
    Locale attributeLocale;
    for(String attributeName : attributeNames) {
      String cellValueAsString = getCellValueAsString(attributesRow.getCell(headerMap.get(attributeName)));
      if(cellValueAsString.length() == 0) {
        continue;
      }

      attributeLocale = getAttributeLocale(attributeName);
      if(attributeLocale != null) {
        String attributeValue = cellValueAsString;
        builder.addAttribute(getAttributeShortName(attributeName), attributeValue, attributeLocale);
      } else {
        // OPAL-173: removed attributes sheet
        // ValueType attributeType = readCustomAttributeType(attributeAwareType, attributeAwareName, attributeName);
        ValueType attributeType = TextType.get();
        if(attributeType != null) {
          Value attributeValue = attributeType.valueOf(cellValueAsString);
          Attribute.Builder attributeBuilder = Attribute.Builder.newAttribute(getAttributeShortName(attributeName));
          attributeBuilder.withValue(attributeValue);
          builder.addAttribute(attributeBuilder.build());
        }
      }
    }
  }

  //
  // marshall
  //

  public Row marshall(Variable variable, Row headerRowVariables, Row headerRowCategories) {
    Row variableRow = getVariableRow(variable);

    ExcelUtil.setCellValue(getVariableCell(variableRow, "table"), TextType.get(), valueTable.getName());
    ExcelUtil.setCellValue(getVariableCell(variableRow, "name"), TextType.get(), variable.getName());
    ExcelUtil.setCellValue(getVariableCell(variableRow, "mimeType"), TextType.get(), variable.getMimeType());
    ExcelUtil.setCellValue(getVariableCell(variableRow, "occurrenceGroup"), TextType.get(), variable.getOccurrenceGroup());
    ExcelUtil.setCellValue(getVariableCell(variableRow, "entityType"), TextType.get(), variable.getEntityType());
    ExcelUtil.setCellValue(getVariableCell(variableRow, "unit"), TextType.get(), variable.getUnit());
    ExcelUtil.setCellValue(getVariableCell(variableRow, "repeatable"), BooleanType.get(), variable.isRepeatable());
    ExcelUtil.setCellValue(getVariableCell(variableRow, "valueType"), TextType.get(), variable.getValueType().getName());

    marshallCustomAttributes(variable, "variable", variable.getName(), variableRow, headerRowVariables, headerMapVariables);

    for(Category category : variable.getCategories()) {
      marshallCategory(variable, category, headerRowCategories);
    }

    return variableRow;
  }

  private void marshallCategory(Variable variable, Category category, Row headerRowCategories) {
    Row categoryRow = getCategoryRow(variable, category);

    ExcelUtil.setCellValue(getCategoryCell(categoryRow, "table"), TextType.get(), valueTable.getName());
    ExcelUtil.setCellValue(getCategoryCell(categoryRow, "variable"), TextType.get(), variable.getName());
    ExcelUtil.setCellValue(getCategoryCell(categoryRow, "name"), TextType.get(), category.getName());
    ExcelUtil.setCellValue(getCategoryCell(categoryRow, "code"), TextType.get(), category.getCode());
    ExcelUtil.setCellValue(getCategoryCell(categoryRow, "missing"), BooleanType.get(), category.isMissing());

    marshallCustomAttributes(category, "category", category.getName(), categoryRow, headerRowCategories, headerMapCategories);
  }

  /**
   * Writes the custom Attributes of an AttributeAware instance (ex: Variable, Category...) to a Row in an Excel sheet.
   * 
   * @param variable
   * @param headerRow
   * @param headerMap
   * @param attributesRow
   */
  private void marshallCustomAttributes(AttributeAware attributeAware, String attributeAwareType, String attributeAwareName, Row attributesRow, Row headerRow, Map<String, Integer> headerMap) {
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
        headerCell.setCellStyle(valueTable.getDatasource().getHeaderCellStyle());
      }
      ExcelUtil.setCellValue(attributesRow.getCell(attributeCellIndex, Row.CREATE_NULL_AS_BLANK), customAttribute.getValue());
      stringBuilder.setLength(0);
    }
  }

  //
  // utility methods
  //

  /**
   * Returns the {@code Row} from the variable sheet for the specified variable. If no such row currently exists, a new
   * one is added and returned.
   * @param variable
   * @return
   */
  private Row getVariableRow(Variable variable) {
    Row row = variableRows.get(variable.getName());
    if(row == null) {
      Sheet variables = valueTable.getDatasource().getVariablesSheet();
      row = variables.createRow(variables.getPhysicalNumberOfRows());
      variableRows.put(variable.getName(), row);
    }
    return row;
  }

  /**
   * Returns the {@code Row} from the variable sheet for the specified variable. If no such row currently exists, a new
   * one is added and returned.
   * @param variable
   * @return
   */
  private Row getCategoryRow(Variable variable, Category category) {
    String key = variable.getName() + category.getName();
    Row row = categoryRows.get(key);
    if(row == null) {
      Sheet categories = valueTable.getDatasource().getCategoriesSheet();
      row = categories.createRow(categories.getPhysicalNumberOfRows());
      categoryRows.put(key, row);
    }
    return row;
  }

  private int getLastCellNum(Row row) {
    return row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
  }

  private String getCellValueAsString(Cell cell) {
    return ExcelUtil.getCellValueAsString(cell);
  }

  private static Locale getAttributeLocale(String attributeName) {
    String[] parsedAttributeName = attributeName.split(":");
    if(parsedAttributeName.length > 1) {
      return new Locale(parsedAttributeName[1]);
    }
    return null;
  }

  private static String getAttributeShortName(String attributeName) {
    return attributeName.split(":")[0];
  }

  private Cell getVariableCell(Row row, String header) {
    return row.getCell(getHeaderMapVariables().get(header), Row.CREATE_NULL_AS_BLANK);
  }

  private String getVariableCellValue(Row row, String header) {
    return getCellValueAsString(row.getCell(getHeaderMapVariables().get(header))).trim();
  }

  public Map<String, Integer> getHeaderMapVariables() {
    if(headerMapVariables == null) {
      headerMapVariables = valueTable.getDatasource().getVariablesHeaderMap();
    }
    return headerMapVariables;
  }

  private Set<String> getAttributeNamesVariables() {
    if(attributeNamesVariables == null) {
      attributeNamesVariables = valueTable.getDatasource().getVariablesCustomAttributeNames();
    }
    return attributeNamesVariables;
  }

  private String getCategoryCellValue(Row row, String header) {
    return getCellValueAsString(row.getCell(getHeaderMapCategories().get(header))).trim();
  }

  private Cell getCategoryCell(Row row, String header) {
    return row.getCell(getHeaderMapCategories().get(header), Row.CREATE_NULL_AS_BLANK);
  }

  public Map<String, Integer> getHeaderMapCategories() {
    if(headerMapCategories == null) {
      headerMapCategories = valueTable.getDatasource().getCategoriesHeaderMap();
    }
    return headerMapCategories;
  }

  private Set<String> getAttributeNamesCategories() {
    if(attributeNamesCategories == null) {
      attributeNamesCategories = valueTable.getDatasource().getCategoriesCustomAttributeNames();
    }
    return attributeNamesCategories;
  }
}

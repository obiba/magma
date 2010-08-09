package org.obiba.magma.datasource.excel.support;

import java.util.List;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VariableConverter {

  public static final String TABLE = "table";

  public static final String NAME = "name";

  public static final String VALUE_TYPE = "valueType";

  public static final String ENTITY_TYPE = "entityType";

  public static final String MIME_TYPE = "mimeType";

  public static final String UNIT = "unit";

  public static final String REPEATABLE = "repeatable";

  public static final String OCCURRENCE_GROUP = "occurrenceGroup";

  public static final String VARIABLE = "variable";

  public static final String CODE = "code";

  public static final String MISSING = "missing";

  public static final List<String> reservedVariableHeaders = Lists.newArrayList(NAME, //
  VALUE_TYPE, //
  ENTITY_TYPE, //
  MIME_TYPE, //
  UNIT, //
  REPEATABLE, //
  OCCURRENCE_GROUP);

  public static final List<String> reservedCategoryHeaders = Lists.newArrayList(NAME, //
  TABLE, //
  VARIABLE, //
  CODE, //
  MISSING);

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
    String name = getVariableCellValue(variableRow, NAME).trim();
    String valueType = getVariableCellValue(variableRow, VALUE_TYPE).trim();
    String entityType = getVariableCellValue(variableRow, ENTITY_TYPE).trim();
    String mimeType = getVariableCellValue(variableRow, MIME_TYPE).trim();
    String unit = getVariableCellValue(variableRow, UNIT).trim();
    String occurrenceGroup = getVariableCellValue(variableRow, OCCURRENCE_GROUP).trim();
    boolean repeatable = parseBoolean(getVariableCellValue(variableRow, REPEATABLE).trim());

    // default values
    if(valueType.length() == 0 || valueType.equalsIgnoreCase("string")) {
      valueType = TextType.get().getName();
    }
    if(entityType.length() == 0) {
      entityType = "Participant";
    }

    Variable.Builder builder = Variable.Builder.newVariable(name, ValueType.Factory.forName(valueType), entityType);

    // more default values
    if(mimeType.length() > 0) {
      builder.mimeType(mimeType);
    }
    if(unit.length() > 0) {
      builder.unit(unit);
    }
    if(occurrenceGroup.length() > 0) {
      builder.occurrenceGroup(occurrenceGroup);
    }
    if(repeatable) {
      builder.repeatable();
    }

    unmarshallCustomAttributes(name, variableRow, getHeaderMapVariables(), getAttributeNamesVariables(), builder);

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
      categoryTable = getCategoryCellValue(categoryRow, TABLE);
      categoryVariable = getCategoryCellValue(categoryRow, VARIABLE);

      if(categoryTable.equals(valueTable.getName()) && categoryVariable.equals(variableName)) {
        Category category = unmarshallCategory(categoryRow);
        variableBuilder.addCategory(category);
        String key = variableName + category.getName();
        categoryRows.put(key, categoryRow);
      }
    }
  }

  private Category unmarshallCategory(Row categoryRow) {
    String name = getCategoryCellValue(categoryRow, NAME).trim();
    String code = getCategoryCellValue(categoryRow, CODE).trim();
    boolean missing = parseBoolean(getCategoryCellValue(categoryRow, MISSING).trim());

    Category.Builder builder = Category.Builder.newCategory(name).missing(missing);

    // default values
    if(code.length() > 0) {
      builder.withCode(code);
    }

    unmarshallCustomAttributes(name, categoryRow, getHeaderMapCategories(), getAttributeNamesCategories(), builder);

    return ((Category.Builder) builder).build();
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
  private void unmarshallCustomAttributes(String attributeAwareName, Row attributesRow, Map<String, Integer> headerMap, Set<String> attributeNames, AttributeAwareBuilder<?> builder) {
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

    ExcelUtil.setCellValue(getVariableCell(variableRow, TABLE), TextType.get(), valueTable.getName());
    ExcelUtil.setCellValue(getVariableCell(variableRow, NAME), TextType.get(), variable.getName());
    ExcelUtil.setCellValue(getVariableCell(variableRow, MIME_TYPE), TextType.get(), variable.getMimeType());
    ExcelUtil.setCellValue(getVariableCell(variableRow, OCCURRENCE_GROUP), TextType.get(), variable.getOccurrenceGroup());
    ExcelUtil.setCellValue(getVariableCell(variableRow, ENTITY_TYPE), TextType.get(), variable.getEntityType());
    ExcelUtil.setCellValue(getVariableCell(variableRow, UNIT), TextType.get(), variable.getUnit());
    ExcelUtil.setCellValue(getVariableCell(variableRow, REPEATABLE), BooleanType.get(), variable.isRepeatable());
    ExcelUtil.setCellValue(getVariableCell(variableRow, VALUE_TYPE), TextType.get(), variable.getValueType().getName());

    marshallCustomAttributes(variable, variable.getName(), variableRow, headerRowVariables, headerMapVariables);

    for(Category category : variable.getCategories()) {
      marshallCategory(variable, category, headerRowCategories);
    }

    return variableRow;
  }

  private void marshallCategory(Variable variable, Category category, Row headerRowCategories) {
    Row categoryRow = getCategoryRow(variable, category);

    ExcelUtil.setCellValue(getCategoryCell(categoryRow, TABLE), TextType.get(), valueTable.getName());
    ExcelUtil.setCellValue(getCategoryCell(categoryRow, VARIABLE), TextType.get(), variable.getName());
    ExcelUtil.setCellValue(getCategoryCell(categoryRow, NAME), TextType.get(), category.getName());
    ExcelUtil.setCellValue(getCategoryCell(categoryRow, CODE), TextType.get(), category.getCode());
    ExcelUtil.setCellValue(getCategoryCell(categoryRow, MISSING), BooleanType.get(), category.isMissing());

    marshallCustomAttributes(category, category.getName(), categoryRow, headerRowCategories, headerMapCategories);
  }

  /**
   * Writes the custom Attributes of an AttributeAware instance (ex: Variable, Category...) to a Row in an Excel sheet.
   * 
   * @param variable
   * @param headerRow
   * @param headerMap
   * @param attributesRow
   */
  private void marshallCustomAttributes(AttributeAware attributeAware, String attributeAwareName, Row attributesRow, Row headerRow, Map<String, Integer> headerMap) {
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

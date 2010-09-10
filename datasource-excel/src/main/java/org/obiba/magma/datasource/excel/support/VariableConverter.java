package org.obiba.magma.datasource.excel.support;

import java.util.ArrayList;
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
import org.obiba.magma.datasource.excel.ExcelDatasource;
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

  public static final List<String> reservedVariableHeaders = Lists.newArrayList(TABLE, //
  NAME, //
  VALUE_TYPE, //
  ENTITY_TYPE, //
  MIME_TYPE, //
  UNIT, //
  REPEATABLE, //
  OCCURRENCE_GROUP);

  public static final List<String> reservedCategoryHeaders = Lists.newArrayList(TABLE, //
  VARIABLE, //
  NAME, //
  CODE, //
  MISSING);

  private Map<String, Integer> headerMapVariables;

  private Map<String, Integer> headerMapCategories;

  private Map<String, Integer> cachedHeaderMapVariables = Maps.newHashMap();

  private Map<String, Integer> cachedHeaderMapCategories = Maps.newHashMap();

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
    Integer idx = getVariableHeaderIndex(TABLE);
    String table = ExcelDatasource.DEFAULT_TABLE_NAME;
    if(idx != null) {
      table = getVariableCellValue(variableRow, TABLE);
    }
    return valueTable.getName().equals(table);
  }

  public String getVariableName(Row variableRow) {
    return getVariableCellValue(variableRow, NAME).trim();
  }

  //
  // unmarshall
  //

  public Variable unmarshall(Row variableRow) {
    String name = getVariableCellValue(variableRow, NAME).trim();
    String valueTypeStr = getVariableCellValue(variableRow, VALUE_TYPE).trim();
    String entityType = getVariableCellValue(variableRow, ENTITY_TYPE).trim();
    String mimeType = getVariableCellValue(variableRow, MIME_TYPE).trim();
    String unit = getVariableCellValue(variableRow, UNIT).trim();
    String occurrenceGroup = getVariableCellValue(variableRow, OCCURRENCE_GROUP).trim();
    boolean repeatable = parseBoolean(getVariableCellValue(variableRow, REPEATABLE).trim());

    // required values
    if(name.length() == 0) {
      throw new ExcelDatasourceParsingException("Variable name is required in table: " + valueTable.getName(), //
      "VariableNameRequired", ExcelDatasource.VARIABLES_SHEET, variableRow.getRowNum() + 1, valueTable.getName());
    }
    if(name.contains(":")) {
      throw new ExcelDatasourceParsingException("Variable name cannot contain ':' in variable: " + valueTable.getName() + " / " + name, //
      "VariableNameCannotContainColon", ExcelDatasource.VARIABLES_SHEET, variableRow.getRowNum() + 1, valueTable.getName(), name);
    }

    // default values
    ValueType valueType;
    if(valueTypeStr.length() == 0 || valueTypeStr.equalsIgnoreCase("string")) {
      valueType = TextType.get();
    } else {
      try {
        valueType = ValueType.Factory.forName(valueTypeStr);
      } catch(Exception e) {
        throw new ExcelDatasourceParsingException("Unknown value type '" + valueTypeStr + "' for variable: " + valueTable.getName() + " / " + name, //
        "UnknownValueType", ExcelDatasource.VARIABLES_SHEET, variableRow.getRowNum() + 1, valueTable.getName(), name, valueTypeStr);
      }
    }
    if(entityType.length() == 0) {
      entityType = "Participant";
    }

    Variable.Builder builder = Variable.Builder.newVariable(name, valueType, entityType);

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
   * @param variableCategoriesCache
   */
  private void unmarshallCategories(String variableName, Variable.Builder variableBuilder) {
    if(getHeaderMapCategories() == null) return;

    Sheet categoriesSheet = valueTable.getDatasource().getCategoriesSheet();
    List<Integer> variableCategoryRows = valueTable.getVariableCategoryRows(variableName);
    List<String> categoryNames = new ArrayList<String>();
    List<ExcelDatasourceParsingException> errors = new ArrayList<ExcelDatasourceParsingException>();
    Row firstRow = null;

    for(int rowIndex : variableCategoryRows) {
      Row categoryRow = categoriesSheet.getRow(rowIndex);

      if(firstRow == null) firstRow = categoryRow;
      try {
        Category category = unmarshallCategory(variableName, categoryRow);
        if(categoryNames.contains(category.getName())) {
          errors.add(new ExcelDatasourceParsingException("Duplicate category name in variable: " + valueTable.getName() + " / " + variableName, //
          "DuplicateCategoryName", ExcelDatasource.CATEGORIES_SHEET, categoryRow.getRowNum() + 1, valueTable.getName(), variableName, category.getName()));
        } else {
          categoryNames.add(category.getName());
          variableBuilder.addCategory(category);
          String key = variableName + category.getName();
          categoryRows.put(key, categoryRow);
        }
      } catch(ExcelDatasourceParsingException pe) {
        errors.add(pe);
      } catch(Exception e) {
        errors.add(new ExcelDatasourceParsingException("Unexpected error in category: " + e.getMessage(), e, //
        "UnexpectedErrorInCategory", ExcelDatasource.CATEGORIES_SHEET, categoryRow.getRowNum() + 1, valueTable.getName(), variableName));
      }

    }

    if(errors.size() > 0) {
      ExcelDatasourceParsingException parent = new ExcelDatasourceParsingException("Errors while parsing categories of variable: " + valueTable.getName() + " / " + variableName, //
      "VariableCategoriesDefinitionErrors", ExcelDatasource.CATEGORIES_SHEET, firstRow.getRowNum() + 1, valueTable.getName(), variableName);
      parent.setChildren(errors);
      throw parent;
    }
  }

  private Category unmarshallCategory(String variableName, Row categoryRow) {
    String name = getCategoryCellValue(categoryRow, NAME).trim();
    String code = getCategoryCellValue(categoryRow, CODE).trim();
    boolean missing = parseBoolean(getCategoryCellValue(categoryRow, MISSING).trim());

    // required values
    if(name.length() == 0) {
      throw new ExcelDatasourceParsingException("Category name is required for variable: " + valueTable.getName() + " / " + variableName, //
      "CategoryNameRequired", ExcelDatasource.CATEGORIES_SHEET, categoryRow.getRowNum() + 1, valueTable.getName(), variableName);
    }

    Category.Builder builder = Category.Builder.newCategory(name).missing(missing);

    // default values
    if(code.length() > 0) {
      builder.withCode(code);
    }

    unmarshallCustomAttributes(name, categoryRow, getHeaderMapCategories(), getAttributeNamesCategories(), builder);

    return ((Category.Builder) builder).build();
  }

  private boolean parseBoolean(String str) {
    return (str.equals("1") || str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("y") || str.equalsIgnoreCase("x"));
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

  /**
   * Get the index of the last cell in the row.
   * @param row
   * @return 0 if there is no cell
   */
  private int getLastCellNum(Row row) {
    return row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
  }

  /**
   * Get the value in the cell as a string.
   * @param cell
   * @return empty string if cell is null
   */
  private String getCellValueAsString(Cell cell) {
    return ExcelUtil.getCellValueAsString(cell);
  }

  /**
   * Get the locale part in an attribute header such as 'name:locale'.
   * @param attributeName
   * @return null if no locale for this header
   */
  private static Locale getAttributeLocale(String attributeName) {
    String[] parsedAttributeName = attributeName.split(":");
    if(parsedAttributeName.length > 1) {
      return new Locale(parsedAttributeName[1]);
    }
    return null;
  }

  /**
   * Get the name part in an attribute header such as 'name:locale'.
   * @param attributeName
   * @return
   */
  private static String getAttributeShortName(String attributeName) {
    return attributeName.split(":")[0];
  }

  /**
   * Get the variable cell for given row at given header column. Create one if missing.
   * @param row
   * @param header
   * @return null if no such header
   */
  private Cell getVariableCell(Row row, String header) {
    Integer idx = getVariableHeaderIndex(header);
    return idx != null ? row.getCell(idx, Row.CREATE_NULL_AS_BLANK) : null;
  }

  /**
   * Get the variable cell value for given row at given header.
   * @param row
   * @param header
   * @return empty string if no such cell
   */
  private String getVariableCellValue(Row row, final String header) {
    Integer idx = getVariableHeaderIndex(header);
    return idx != null ? getCellValueAsString(row.getCell(idx)).trim() : "";
  }

  /**
   * Get the 0-based index of the variable column at the given header.
   * @param header
   * @return null if no such header
   */
  public Integer getVariableHeaderIndex(final String header) {
    if(reservedVariableHeaders.contains(header)) {
      return getHeaderIndex(getHeaderMapVariables(), cachedHeaderMapVariables, header);
    } else {
      return getHeaderMapVariables().get(header);
    }
  }

  /**
   * Set the 0-based index of a variable column at the given header.
   * @param header
   * @param idx
   */
  public void putVariableHeaderIndex(final String header, final Integer idx) {
    getHeaderMapVariables().put(header, idx);
  }

  /**
   * Get the map between a variable header and the 0-based index of the column.
   * @return
   */
  private Map<String, Integer> getHeaderMapVariables() {
    if(headerMapVariables == null) {
      headerMapVariables = valueTable.getDatasource().getVariablesHeaderMap();
    }
    return headerMapVariables;
  }

  /**
   * Get the set of attribute headers for the variables.
   * @return
   */
  private Set<String> getAttributeNamesVariables() {
    if(attributeNamesVariables == null) {
      attributeNamesVariables = valueTable.getDatasource().getVariablesCustomAttributeNames();
    }
    return attributeNamesVariables;
  }

  /**
   * Get the category cell for given row at given header column. Create one if missing.
   * @param row
   * @param header
   * @return null if no such header
   */
  private Cell getCategoryCell(Row row, String header) {
    Integer idx = getCategoryHeaderIndex(header);
    return idx != null ? row.getCell(idx, Row.CREATE_NULL_AS_BLANK) : null;
  }

  /**
   * Get the category cell value for given row at given header.
   * @param row
   * @param header
   * @return empty string if no such cell
   */
  private String getCategoryCellValue(Row row, String header) {
    Integer idx = getCategoryHeaderIndex(header);
    return idx != null ? getCellValueAsString(row.getCell(idx)).trim() : "";
  }

  /**
   * Get the 0-based index of the category column at the given header.
   * @param header
   * @return null if no such header
   */
  public Integer getCategoryHeaderIndex(final String header) {
    if(reservedCategoryHeaders.contains(header)) {
      return getHeaderIndex(getHeaderMapCategories(), cachedHeaderMapCategories, header);
    } else {
      return getHeaderMapCategories().get(header);
    }
  }

  /**
   * Set the 0-based index of a category column at the given header.
   * @param header
   * @param idx
   */
  public void putCategoryHeaderIndex(final String header, final Integer idx) {
    getHeaderMapCategories().put(header, idx);
  }

  /**
   * Get the 0-based index of the column at the given header relatively to the header map.
   * @param headerMap
   * @param cachedHeaderMap
   * @param header
   * @return null if no such header
   * @see ExcelUtil#findNormalizedHeader(Iterable, String)
   */
  private Integer getHeaderIndex(final Map<String, Integer> headerMap, Map<String, Integer> cachedHeaderMap, final String header) {
    Integer idx = cachedHeaderMap.get(header);
    if(idx == null) {
      String found = ExcelUtil.findNormalizedHeader(headerMap.keySet(), header);
      if(found != null) {
        idx = headerMap.get(found);
        if(idx != null) {
          cachedHeaderMap.put(header, idx);
        }
      }
    }
    return idx;
  }

  /**
   * Get the map between a category header and the 0-based index of the column.
   * @return
   */
  private Map<String, Integer> getHeaderMapCategories() {
    if(headerMapCategories == null) {
      headerMapCategories = valueTable.getDatasource().getCategoriesHeaderMap();
    }
    return headerMapCategories;
  }

  /**
   * Get the set of attribute headers for the categories.
   * @return
   */
  private Set<String> getAttributeNamesCategories() {
    if(attributeNamesCategories == null) {
      attributeNamesCategories = valueTable.getDatasource().getCategoriesCustomAttributeNames();
    }
    return attributeNamesCategories;
  }

  /**
   * Get the table name for a category row.
   * @param categoryRow
   * @return if no table column is defined, returns the default table name
   */
  public String getCategoryTableName(Row categoryRow) {
    Integer idx = getVariableHeaderIndex(TABLE);
    if(idx != null) {
      return getCategoryCellValue(categoryRow, TABLE).trim();
    } else {
      return ExcelDatasource.DEFAULT_TABLE_NAME;
    }
  }

  /**
   * Get the variable name for a category row.
   * @param categoryRow
   * @return
   */
  public String getCategoryVariableName(Row categoryRow) {
    return getCategoryCellValue(categoryRow, VARIABLE).trim();
  }
}

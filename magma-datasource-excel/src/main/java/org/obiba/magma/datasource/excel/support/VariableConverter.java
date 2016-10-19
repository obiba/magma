/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.excel.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.Attributes;
import org.obiba.magma.Category;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.datasource.excel.ExcelValueTable;
import org.obiba.magma.datasource.excel.ExcelValueTableWriter;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VariableConverter {

  public static final String TABLE = "table";

  public static final String NAME = "name";

  public static final String VALUE_TYPE = "valueType";

  public static final String ENTITY_TYPE = "entityType";

  public static final String REFERENCED_ENTITY_TYPE = "referencedEntityType";

  public static final String MIME_TYPE = "mimeType";

  public static final String UNIT = "unit";

  public static final String INDEX = "index";

  public static final String REPEATABLE = "repeatable";

  public static final String OCCURRENCE_GROUP = "occurrenceGroup";

  public static final String VARIABLE = "variable";

  public static final String CODE = "code";

  public static final String MISSING = "missing";

  public static final List<String> reservedVariableHeaders = Lists.newArrayList(TABLE, //
      NAME, //
      VALUE_TYPE, //
      ENTITY_TYPE, //
      REFERENCED_ENTITY_TYPE, //
      MIME_TYPE, //
      UNIT, //
      REPEATABLE, //
      OCCURRENCE_GROUP, //
      INDEX);

  public static final List<String> reservedCategoryHeaders = Lists.newArrayList(TABLE, //
      VARIABLE, //
      NAME, //
      CODE, //
      MISSING);

  private Map<String, Integer> headerMapVariables;

  private Map<String, Integer> headerMapCategories;

  private final Map<String, Integer> cachedHeaderMapVariables = Maps.newHashMap();

  private final Map<String, Integer> cachedHeaderMapCategories = Maps.newHashMap();

  private Set<String> attributeNamesCategories;

  private Set<String> attributeNamesVariables;

  /**
   * Maps a variable's name to its Row in the variablesSheet
   */
  private final Map<String, Row> variableRows;

  /**
   * Maps a category's name concatenated with the variable's name to its Row in the variablesSheet
   */
  private final Map<String, Row> categoryRows;

  private final ExcelValueTable valueTable;

  public VariableConverter(ExcelValueTable valueTable) {
    this.valueTable = valueTable;
    variableRows = Maps.newHashMap();
    categoryRows = Maps.newHashMap();
  }

  /**
   * Check if is a variable row for the current table.
   *
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
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public Variable unmarshall(Row variableRow) {
    String tableName = valueTable.getName();
    int rowNum = variableRow.getRowNum();

    String name = getVariableCellValue(variableRow, NAME).trim();
    if(name.isEmpty()) {
      throw new ExcelDatasourceParsingException("Variable name is required in table: " + tableName, //
          "VariableNameRequired", ExcelDatasource.VARIABLES_SHEET, rowNum + 1, tableName);
    }
    if(name.contains(":")) {
      throw new ExcelDatasourceParsingException(
          "Variable name cannot contain ':' in variable: " + tableName + " / " + name, //
          "VariableNameCannotContainColon", ExcelDatasource.VARIABLES_SHEET, rowNum + 1, tableName, name);
    }

    String entityType = getVariableCellValue(variableRow, ENTITY_TYPE).trim();
    if(entityType.isEmpty()) entityType = "Participant";
    ValueType valueType = unmarshallValueType(variableRow, name, tableName, rowNum);

    Variable.Builder builder = Variable.Builder.newVariable(name, valueType, entityType);
    unmarshallMimeType(variableRow, builder);
    unmarshallUnit(variableRow, builder);
    unmarshallIndex(variableRow, builder);
    unmarshallOccurrenceGroup(variableRow, builder);
    unmarshallRepeatable(variableRow, builder);
    unmarshallReferencedEntityType(variableRow, builder);
    unmarshallCustomAttributes(variableRow, getHeaderMapVariables(), getAttributeNamesVariables(), builder);
    unmarshallCategories(name, builder);
    variableRows.put(name, variableRow);
    return builder.build();
  }

  private void unmarshallReferencedEntityType(Row variableRow, Variable.Builder builder) {
    String referencedEntityType = getVariableCellValue(variableRow, REFERENCED_ENTITY_TYPE).trim();
    if(referencedEntityType.length() > 0) {
      builder.referencedEntityType(referencedEntityType);
    }
  }

  private void unmarshallRepeatable(Row variableRow, Variable.Builder builder) {
    boolean repeatable = parseBoolean(getVariableCellValue(variableRow, REPEATABLE).trim());
    if(repeatable) {
      builder.repeatable();
    }
  }

  private void unmarshallOccurrenceGroup(Row variableRow, Variable.Builder builder) {
    String occurrenceGroup = getVariableCellValue(variableRow, OCCURRENCE_GROUP).trim();
    if(occurrenceGroup.length() > 0) {
      builder.occurrenceGroup(occurrenceGroup);
    }
  }

  private void unmarshallUnit(Row variableRow, Variable.Builder builder) {
    String unit = getVariableCellValue(variableRow, UNIT).trim();
    if(unit.length() > 0) {
      builder.unit(unit);
    }
  }

  private void unmarshallIndex(Row variableRow, Variable.Builder builder) {
    String index = getVariableCellValue(variableRow, INDEX).trim();
    if(index.length() > 0) {
      try {
        builder.index(Integer.valueOf(index));
      } catch(NumberFormatException e) {
        // ignore
      }
    }
  }

  private void unmarshallMimeType(Row variableRow, Variable.Builder builder) {
    String mimeType = getVariableCellValue(variableRow, MIME_TYPE).trim();
    if(mimeType.length() > 0) {
      builder.mimeType(mimeType);
    }
  }

  private ValueType unmarshallValueType(Row variableRow, String name, String tableName, int rowNum) {
    ValueType valueType;
    String valueTypeStr = getVariableCellValue(variableRow, VALUE_TYPE).trim();
    if(valueTypeStr.isEmpty() || "string".equalsIgnoreCase(valueTypeStr)) {
      valueType = TextType.get();
    } else {
      try {
        valueType = ValueType.Factory.forName(valueTypeStr);
      } catch(Exception e) {
        throw new ExcelDatasourceParsingException(
            "Unknown value type '" + valueTypeStr + "' for variable: " + tableName + " / " + name, //
            "UnknownValueType", ExcelDatasource.VARIABLES_SHEET, rowNum + 1, tableName, name, valueTypeStr);
      }
    }
    return valueType;
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
    Collection<String> categoryNames = new ArrayList<>();
    Collection<ExcelDatasourceParsingException> errors = new ArrayList<>();
    Row firstRow = null;
    for(int rowIndex : variableCategoryRows) {
      Row categoryRow = categoriesSheet.getRow(rowIndex);
      if(firstRow == null) firstRow = categoryRow;
      unmarshallCategories(variableName, variableBuilder, categoryNames, errors, categoryRow);
    }

    if(errors.size() > 0) {
      ExcelDatasourceParsingException parent = new ExcelDatasourceParsingException(
          "Errors while parsing categories of variable: " + valueTable.getName() + " / " + variableName,
          "VariableCategoriesDefinitionErrors", ExcelDatasource.CATEGORIES_SHEET, firstRow.getRowNum() + 1,
          valueTable.getName(), variableName);
      parent.setChildren(errors);
      throw parent;
    }
  }

  private void unmarshallCategories(String variableName, Variable.Builder variableBuilder,
      Collection<String> categoryNames, Collection<ExcelDatasourceParsingException> errors, Row categoryRow) {
    try {
      Category category = unmarshallCategory(variableName, categoryRow);
      if(categoryNames.contains(category.getName())) {
        errors.add(new ExcelDatasourceParsingException(
            "Duplicate category name in variable: " + valueTable.getName() + " / " + variableName,
            "DuplicateCategoryName", ExcelDatasource.CATEGORIES_SHEET, categoryRow.getRowNum() + 1,
            valueTable.getName(), variableName, category.getName()));
      } else {
        categoryNames.add(category.getName());
        variableBuilder.addCategory(category);
        String key = variableName + category.getName();
        categoryRows.put(key, categoryRow);
      }
    } catch(ExcelDatasourceParsingException pe) {
      errors.add(pe);
    } catch(Exception e) {
      errors.add(new ExcelDatasourceParsingException("Unexpected error in category: " + e.getMessage(), e,
          "UnexpectedErrorInCategory", ExcelDatasource.CATEGORIES_SHEET, categoryRow.getRowNum() + 1,
          valueTable.getName(), variableName));
    }
  }

  private Category unmarshallCategory(String variableName, Row categoryRow) {
    String name = getCategoryCellValue(categoryRow, NAME).trim();
    String code = getCategoryCellValue(categoryRow, CODE).trim();
    boolean missing = parseBoolean(getCategoryCellValue(categoryRow, MISSING).trim());

    // required values
    if(name.isEmpty()) {
      throw new ExcelDatasourceParsingException(
          "Category name is required for variable: " + valueTable.getName() + " / " + variableName,
          "CategoryNameRequired", ExcelDatasource.CATEGORIES_SHEET, categoryRow.getRowNum() + 1, valueTable.getName(),
          variableName);
    }

    Category.Builder builder = Category.Builder.newCategory(name).missing(missing);

    // default values
    if(code.length() > 0) {
      builder.withCode(code);
    }

    unmarshallCustomAttributes(categoryRow, getHeaderMapCategories(), getAttributeNamesCategories(), builder);

    return builder.build();
  }

  private boolean parseBoolean(String str) {
    return "1".equals(str) || "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str) ||
        "y".equalsIgnoreCase(str) || "x".equalsIgnoreCase(str);
  }

  /**
   * Read the custom Attributes define by a Row in an Excel sheet to an AttributeAware instance (ex: Variable,
   * Category...) using an AttributeAwareBuilder.
   *
   * @param headerMap
   * @param attributeNames
   */
  private void unmarshallCustomAttributes(Row attributesRow, Map<String, Integer> headerMap,
      Iterable<String> attributeNames, AttributeAwareBuilder<?> builder) {
    for(String attributeName : attributeNames) {
      String cellValueAsString = getCellValueAsString(attributesRow.getCell(headerMap.get(attributeName)));
      if(cellValueAsString.isEmpty()) {
        continue;
      }
      Attribute.Builder attr = Attributes.decodeFromHeader(attributeName);
      builder.addAttribute(attr.withValue(cellValueAsString).build());
    }
  }

  //
  // marshall
  //

  public Row marshall(ExcelValueTableWriter.VariableWithMetadata itemToWrite) {

    Variable variable = itemToWrite.getVariable();

    Row variableRow = getVariableRow(variable);

    ExcelUtil.setCellValue(getVariableCell(variableRow, TABLE), TextType.get(), itemToWrite.getTableName());
    ExcelUtil.setCellValue(getVariableCell(variableRow, NAME), TextType.get(), variable.getName());
    ExcelUtil.setCellValue(getVariableCell(variableRow, MIME_TYPE), TextType.get(), variable.getMimeType());
    ExcelUtil
        .setCellValue(getVariableCell(variableRow, OCCURRENCE_GROUP), TextType.get(), variable.getOccurrenceGroup());
    ExcelUtil.setCellValue(getVariableCell(variableRow, ENTITY_TYPE), TextType.get(), variable.getEntityType());
    ExcelUtil.setCellValue(getVariableCell(variableRow, UNIT), TextType.get(), variable.getUnit());
    ExcelUtil.setCellValue(getVariableCell(variableRow, INDEX), IntegerType.get(), variable.getIndex());
    ExcelUtil.setCellValue(getVariableCell(variableRow, REPEATABLE), BooleanType.get(), variable.isRepeatable());
    ExcelUtil.setCellValue(getVariableCell(variableRow, VALUE_TYPE), TextType.get(), variable.getValueType().getName());
    ExcelUtil.setCellValue(getVariableCell(variableRow, REFERENCED_ENTITY_TYPE), TextType.get(),
        variable.getReferencedEntityType());

    marshallCustomAttributes(variable, variableRow, itemToWrite.getHeaderRowVariables(), headerMapVariables);

    for(Category category : variable.getCategories()) {
      marshallCategory(variable, category, itemToWrite.getHeaderRowCategories());
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

    marshallCustomAttributes(category, categoryRow, headerRowCategories, headerMapCategories);
  }

  /**
   * Writes the custom Attributes of an AttributeAware instance (ex: Variable, Category...) to a Row in an Excel sheet.
   *
   * @param attributesRow
   * @param headerRow
   * @param headerMap
   */
  private void marshallCustomAttributes(AttributeAware attributeAware, Row attributesRow, Row headerRow, Map<String, Integer> headerMap) {

    for(Attribute customAttribute : attributeAware.getAttributes()) {
      String headerValue = Attributes.encodeForHeader(customAttribute);
      Integer attributeCellIndex = headerMap.get(headerValue);

      ExcelUtil.setCellValue(attributesRow.getCell(attributeCellIndex, Row.CREATE_NULL_AS_BLANK),
              customAttribute.getValue());
    }
  }

  public void createVariablesHeaders(List<Attribute> attributes, Row headerRow) {
    createHeaders(attributes, headerRow, headerMapVariables);
  }

  public void createCategoriesHeaders(List<Attribute> attributes, Row headerRow) {
    createHeaders(attributes, headerRow, headerMapCategories);
  }

  private void createHeaders(List<Attribute> attributes, Row headerRow, Map<String, Integer> headerMap) {

    List<String> collect = attributes.stream()
            .map(Attributes::encodeForHeader)
            .distinct()
            .sorted(new FullQualifiedAttributeComparator())
            .collect(Collectors.toList());

    for (String headerValue : collect) {
      Integer attributeCellIndex = headerMap.get(headerValue);
      if(attributeCellIndex == null) {
        headerMap.put(headerValue, getLastCellNum(headerRow));
        attributeCellIndex = getLastCellNum(headerRow);
        Cell headerCell = headerRow.createCell(attributeCellIndex);
        headerCell.setCellValue(headerValue);
        headerCell.setCellStyle(valueTable.getDatasource().getHeaderCellStyle());
      }
    }
  }

  //
  // utility methods
  //

  private class FullQualifiedAttributeComparator implements Comparator<String> {

    private static final String DELIMITER = "::";

    @Override
    public int compare(String o1, String o2) {
      if (hasNamespace(o1) == hasNamespace(o2))
        return o1.compareTo(o2);
      else if (hasNamespace(o1))
        return 1;
      else
        return -1;
    }

    private boolean hasNamespace(String o1) {
      return o1.contains(DELIMITER);
    }
  }

  /**
   * Returns the {@code Row} from the variable sheet for the specified variable. If no such row currently exists, a new
   * one is added and returned.
   *
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
   *
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
   *
   * @param row
   * @return 0 if there is no cell
   */
  private int getLastCellNum(Row row) {
    return row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
  }

  /**
   * Get the value in the cell as a string.
   *
   * @param cell
   * @return empty string if cell is null
   */
  private String getCellValueAsString(Cell cell) {
    return ExcelUtil.getCellValueAsString(cell);
  }

  /**
   * Get the variable cell for given row at given header column. Create one if missing.
   *
   * @param row
   * @param header
   * @return null if no such header
   */
  @Nullable
  private Cell getVariableCell(Row row, String header) {
    Integer idx = getVariableHeaderIndex(header);
    return idx == null ? null : row.getCell(idx, Row.CREATE_NULL_AS_BLANK);
  }

  /**
   * Get the variable cell value for given row at given header.
   *
   * @param row
   * @param header
   * @return empty string if no such cell
   */
  private String getVariableCellValue(Row row, String header) {
    if (row == null) return "";
    Integer idx = getVariableHeaderIndex(header);
    return idx != null ? getCellValueAsString(row.getCell(idx)).trim() : "";
  }

  /**
   * Get the 0-based index of the variable column at the given header.
   *
   * @param header
   * @return null if no such header
   */
  @Nullable
  public Integer getVariableHeaderIndex(String header) {
    return reservedVariableHeaders.contains(header) //
        ? getHeaderIndex(getHeaderMapVariables(), cachedHeaderMapVariables, header) //
        : getHeaderMapVariables().get(header);
  }

  /**
   * Set the 0-based index of a variable column at the given header.
   *
   * @param header
   * @param idx
   */
  public void putVariableHeaderIndex(String header, Integer idx) {
    getHeaderMapVariables().put(header, idx);
  }

  /**
   * Get the map between a variable header and the 0-based index of the column.
   *
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
   *
   * @return
   */
  private Iterable<String> getAttributeNamesVariables() {
    if(attributeNamesVariables == null) {
      attributeNamesVariables = valueTable.getDatasource().getVariablesCustomAttributeNames();
    }
    return attributeNamesVariables;
  }

  /**
   * Get the category cell for given row at given header column. Create one if missing.
   *
   * @param row
   * @param header
   * @return null if no such header
   */
  @Nullable
  private Cell getCategoryCell(Row row, String header) {
    Integer idx = getCategoryHeaderIndex(header);
    return idx == null || row == null ? null : row.getCell(idx, Row.CREATE_NULL_AS_BLANK);
  }

  /**
   * Get the category cell value for given row at given header.
   *
   * @param row
   * @param header
   * @return empty string if no such cell
   */
  private String getCategoryCellValue(Row row, String header) {
    Integer idx = getCategoryHeaderIndex(header);
    return idx != null && row != null ? getCellValueAsString(row.getCell(idx)).trim() : "";
  }

  /**
   * Get the 0-based index of the category column at the given header.
   *
   * @param header
   * @return null if no such header
   */
  @Nullable
  public Integer getCategoryHeaderIndex(String header) {
    return reservedCategoryHeaders.contains(header) //
        ? getHeaderIndex(getHeaderMapCategories(), cachedHeaderMapCategories, header) //
        : getHeaderMapCategories().get(header);
  }

  /**
   * Set the 0-based index of a category column at the given header.
   *
   * @param header
   * @param idx
   */
  public void putCategoryHeaderIndex(String header, Integer idx) {
    getHeaderMapCategories().put(header, idx);
  }

  /**
   * Get the 0-based index of the column at the given header relatively to the header map.
   *
   * @param headerMap
   * @param cachedHeaderMap
   * @param header
   * @return null if no such header
   * @see ExcelUtil#findNormalizedHeader(Iterable, String)
   */
  @Nullable
  private Integer getHeaderIndex(Map<String, Integer> headerMap, Map<String, Integer> cachedHeaderMap, String header) {
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
   *
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
   *
   * @return
   */
  private Iterable<String> getAttributeNamesCategories() {
    if(attributeNamesCategories == null) {
      attributeNamesCategories = valueTable.getDatasource().getCategoriesCustomAttributeNames();
    }
    return attributeNamesCategories;
  }

  /**
   * Get the table name for a category row.
   *
   * @param categoryRow
   * @return if no table column is defined, returns the default table name
   */
  public String getCategoryTableName(Row categoryRow) {
    Integer idx = getVariableHeaderIndex(TABLE);
    return idx == null ? ExcelDatasource.DEFAULT_TABLE_NAME : getCategoryCellValue(categoryRow, TABLE).trim();
  }

  /**
   * Get the variable name for a category row.
   *
   * @param categoryRow
   * @return
   */
  public String getCategoryVariableName(Row categoryRow) {
    return getCategoryCellValue(categoryRow, VARIABLE).trim();
  }
}

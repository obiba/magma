package org.obiba.magma.datasource.excel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.Category;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.datasource.excel.support.NameConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

class ExcelValueTable extends AbstractValueTable implements Initialisable {

  private static final Logger log = LoggerFactory.getLogger(ExcelValueTable.class);

  private final Sheet valueTableSheet;

  /** Maps a variable's name to its Row in the variablesSheet */
  private final Map<String, Row> variableRows = Maps.newHashMap();

  /** Maps a category's name concatenated with the variable's name to its Row in the variablesSheet */
  private final Map<String, Row> categoryRows = Maps.newHashMap();

  public ExcelValueTable(ExcelDatasource excelDatasource, String name, Sheet sheet, String entityType) {
    super(excelDatasource, NameConverter.toExcelName(name));
    this.valueTableSheet = sheet;

    if(valueTableSheet.getPhysicalNumberOfRows() <= 0) {
      valueTableSheet.createRow(0);
    }

  }

  @Override
  public void initialise() {
    super.initialise();
    try {
      readVariables();
      // printVariables();

    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Override
  public ExcelDatasource getDatasource() {
    return (ExcelDatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    throw new UnsupportedOperationException("getValueSet not supported");
  }

  int findVariableColumn(Variable variable) {
    Row variableNameRow = valueTableSheet.getRow(0);
    for(int i = 0; i < variableNameRow.getPhysicalNumberOfCells(); i++) {
      Cell cell = variableNameRow.getCell(i);
      if(ExcelUtil.getCellValueAsString(cell).equals(variable.getName())) {
        return i;
      }
    }
    return -1;
  }

  int getVariableColumn(Variable variable) {
    int column = findVariableColumn(variable);
    if(column == -1) {
      // Add it
      Row variableNameRow = valueTableSheet.getRow(0);
      Cell variableColumn = variableNameRow.createCell(variableNameRow.getPhysicalNumberOfCells(), Cell.CELL_TYPE_STRING);
      ExcelUtil.setCellValue(variableColumn, TextType.get(), variable.getName());
      variableColumn.setCellStyle(getDatasource().getExcelStyles("headerCellStyle"));
      column = variableColumn.getColumnIndex();
    }
    return column;
  }

  /**
   * Returns the {@code Row} from the variable sheet for the specified variable. If no such row currently exists, a new
   * one is added and returned.
   * @param variable
   * @return
   */
  Row getVariableRow(Variable variable) {
    Row row = variableRows.get(variable.getName());
    if(row == null) {
      Sheet variables = getDatasource().getVariablesSheet();
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
  Row getCategoryRow(Variable variable, Category category) {
    String key = variable.getName() + category.getName();
    Row row = categoryRows.get(key);
    if(row == null) {
      Sheet categories = getDatasource().getCategoriesSheet();
      row = categories.createRow(categories.getPhysicalNumberOfRows());
      categoryRows.put(key, row);
    }
    return row;
  }

  Sheet getValueTableSheet() {
    return valueTableSheet;
  }

  private void printVariables() {
    log.debug("Table (name = {})", getName());
    Iterable<Variable> variables = getVariables();
    for(Variable variable : variables) {
      log.debug("  Variable (name = {})", variable.getName());
      Set<Category> categories = variable.getCategories();
      for(Category category : categories) {
        log.debug("    Category (name = {})\n", category.getName());
      }
    }
  }

  private void readVariables() throws FileNotFoundException, IOException {

    Sheet variablesSheet = getDatasource().getVariablesSheet();

    Row headerVariables = variablesSheet.getRow(0);
    // OPAL-237. This prevents subsequent NPE.
    // TODO: In fact, when the variables sheet is not present, we should use the columns in the Excel sheet to extract
    // variables. This would allow us to read any excel spreadsheet, not just the ones we've created.
    if(headerVariables == null) {
      return;
    }
    Map<String, Integer> headerMapVariables = ExcelDatasource.mapSheetHeader(headerVariables);
    Set<String> attributeNamesVariables = ExcelDatasource.getCustomAttributeNames(headerVariables, ExcelDatasource.variablesReservedAttributeNames);

    Boolean repeatable;
    Row variableRow;

    int variableRowCount = variablesSheet.getPhysicalNumberOfRows();

    for(int i = 1; i < variableRowCount; i++) {
      variableRow = variablesSheet.getRow(i);
      String table = getCellValueAsString(variableRow.getCell(headerMapVariables.get("table")));
      if(table.equals(getName())) {
        String name = getCellValueAsString(variableRow.getCell(headerMapVariables.get("name")));
        String valueType = getCellValueAsString(variableRow.getCell(headerMapVariables.get("valueType")));
        String entityType = getCellValueAsString(variableRow.getCell(headerMapVariables.get("entityType")));
        String mimeType = getCellValueAsString(variableRow.getCell(headerMapVariables.get("mimeType")));
        String unit = getCellValueAsString(variableRow.getCell(headerMapVariables.get("unit")));
        String occurrenceGroup = getCellValueAsString(variableRow.getCell(headerMapVariables.get("occurrenceGroup")));

        Variable.Builder variableBuilder = Variable.Builder.newVariable(name, ValueType.Factory.forName(valueType), entityType).mimeType(mimeType).unit(unit).occurrenceGroup(occurrenceGroup);
        repeatable = getCellValueAsString(variableRow.getCell(headerMapVariables.get("repeatable"))) == "1";

        if(repeatable) {
          variableBuilder.repeatable();
        }

        readCustomAttributes("variable", name, variableRow, headerMapVariables, attributeNamesVariables, variableBuilder);

        readCategories(name, variableBuilder);

        variableRows.put(name, variableRow);

        addVariableValueSource(new ExcelVariableValueSource(variableBuilder.build()));
      }
    }

  }

  /**
   * Read the Categories of a specific Variable in the "Categories" Excel sheet. Add these Categories to the specified
   * Variable using the Variable.Builder.
   * 
   * @param variableName
   * @param variableBuilder
   */
  private void readCategories(String variableName, Variable.Builder variableBuilder) {

    String categoryTable;
    String categoryVariable;
    String categoryName;
    String categoryCode;
    boolean missing;
    Row categoryRow;

    Sheet categoriesSheet = getDatasource().getCategoriesSheet();
    Row headerCategories = categoriesSheet.getRow(0);
    Map<String, Integer> headerMapCategories = ExcelDatasource.mapSheetHeader(headerCategories);
    Set<String> attributeNamesCategories = ExcelDatasource.getCustomAttributeNames(headerCategories, ExcelDatasource.categoriesReservedAttributeNames);

    int categoryRowCount = categoriesSheet.getPhysicalNumberOfRows();
    for(int x = 1; x < categoryRowCount; x++) {
      categoryRow = categoriesSheet.getRow(x);
      categoryTable = getCellValueAsString(categoryRow.getCell(headerMapCategories.get("table")));
      categoryVariable = getCellValueAsString(categoryRow.getCell(headerMapCategories.get("variable")));
      if(categoryTable.equals(getName()) && categoryVariable.equals(variableName)) {
        categoryName = getCellValueAsString(categoryRow.getCell(headerMapCategories.get("name")));
        categoryCode = getCellValueAsString(categoryRow.getCell(headerMapCategories.get("code")));
        missing = getCellValueAsString(categoryRow.getCell(headerMapCategories.get("missing"))) == "1";

        AttributeAwareBuilder<Category.Builder> categoryBuilder = Category.Builder.newCategory(categoryName).withCode(categoryCode).missing(missing);
        readCustomAttributes("category", categoryName, categoryRow, headerMapCategories, attributeNamesCategories, categoryBuilder);
        variableBuilder.addCategory(((Category.Builder) categoryBuilder).build());

        String key = variableName + categoryName;
        categoryRows.put(key, categoryRow);
      }
    }
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
  private void readCustomAttributes(String attributeAwareType, String attributeAwareName, Row attributesRow, Map<String, Integer> headerMap, Set<String> attributeNames, AttributeAwareBuilder<?> builder) {
    Locale attributeLocale;
    for(String attributeName : attributeNames) {
      String cellValueAsString = getCellValueAsString(attributesRow.getCell(headerMap.get(attributeName)));
      if(cellValueAsString.length() == 0) {
        continue;
      }

      attributeLocale = ExcelDatasource.getAttributeLocale(attributeName);
      if(attributeLocale != null) {
        String attributeValue = cellValueAsString;
        builder.addAttribute(ExcelDatasource.getAttributeShortName(attributeName), attributeValue, attributeLocale);
      } else {
        // OPAL-173: removed attributes sheet
        // ValueType attributeType = readCustomAttributeType(attributeAwareType, attributeAwareName, attributeName);
        ValueType attributeType = TextType.get();
        if(attributeType != null) {
          Value attributeValue = attributeType.valueOf(cellValueAsString);
          Attribute.Builder attributeBuilder = Attribute.Builder.newAttribute(ExcelDatasource.getAttributeShortName(attributeName));
          attributeBuilder.withValue(attributeValue);
          builder.addAttribute(attributeBuilder.build());
        }
      }
    }
  }

  private ValueType readCustomAttributeType(String attributeAwareType, String attributeAwareName, String attributeName) {
    Sheet attributesSheet = getDatasource().getAttributesSheet();
    Row headerRow = attributesSheet.getRow(0);
    Map<String, Integer> headerMap = ExcelDatasource.mapSheetHeader(headerRow);

    for(int i = 1; i < attributesSheet.getPhysicalNumberOfRows(); i++) {
      Row row = attributesSheet.getRow(i);
      String cellTable = getCellValueAsString(row.getCell(headerMap.get("table")));
      String cellAttributeAwareType = getCellValueAsString(row.getCell(headerMap.get("attributeAwareType")));
      String cellAttributeAware = getCellValueAsString(row.getCell(headerMap.get("attributeAware")));
      String cellAttribute = getCellValueAsString(row.getCell(headerMap.get("attribute")));
      String cellValueType = getCellValueAsString(row.getCell(headerMap.get("valueType")));

      if(cellTable.equals(getName()) && cellAttributeAwareType.equals(attributeAwareType) && cellAttributeAware.equals(attributeAwareName) && cellAttribute.equals(attributeName)) {
        return ValueType.Factory.forName(cellValueType);
      }
    }

    return null;
  }

  private String getCellValueAsString(Cell cell) {
    return ExcelUtil.getCellValueAsString(cell);
  }
}

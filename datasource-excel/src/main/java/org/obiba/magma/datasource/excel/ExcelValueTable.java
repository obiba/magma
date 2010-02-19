package org.obiba.magma.datasource.excel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.AttributeAwareBuilder;
import org.obiba.magma.Category;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractValueTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExcelValueTable extends AbstractValueTable implements Initialisable, Disposable {

  private static final Logger log = LoggerFactory.getLogger(ExcelValueTable.class);

  public ExcelValueTable(String name, ExcelDatasource datasource, Sheet excelSheet) {
    super(datasource, name);
  }

  public ExcelValueTable(String name, ExcelDatasource datasource, Sheet excelSheet, String entityType) {
    super(datasource, name);
  }

  @Override
  public void initialise() {
    super.initialise();
    try {
      readVariables();
      printVariables();

    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }
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
    Map<String, Integer> headerMapVariables = ExcelDatasource.mapSheetHeader(headerVariables);
    Set<String> attributeNamesVariables = ExcelDatasource.getCustomAttributeNames(headerVariables, ExcelDatasource.variablesReservedAttributeNames);

    String name;
    String valueType;
    String entityType;
    String mimeType;
    String unit;
    String occurrenceGroup;
    Boolean repeatable;
    Row variableRow;

    int variableRowCount = variablesSheet.getPhysicalNumberOfRows();

    for(int i = 1; i < variableRowCount; i++) {
      variableRow = variablesSheet.getRow(i);

      name = getCellValueAsString(variableRow.getCell(headerMapVariables.get("name")));
      valueType = getCellValueAsString(variableRow.getCell(headerMapVariables.get("valueType")));
      entityType = getCellValueAsString(variableRow.getCell(headerMapVariables.get("entityType")));
      mimeType = getCellValueAsString(variableRow.getCell(headerMapVariables.get("mimeType")));
      unit = getCellValueAsString(variableRow.getCell(headerMapVariables.get("unit")));
      occurrenceGroup = getCellValueAsString(variableRow.getCell(headerMapVariables.get("occurrenceGroup")));

      Variable.Builder variableBuilder = Variable.Builder.newVariable(name, ValueType.Factory.forName(valueType), entityType).mimeType(mimeType).unit(unit).occurrenceGroup(occurrenceGroup);
      repeatable = getCellValueAsString(variableRow.getCell(headerMapVariables.get("repeatable"))) == "1";

      if(repeatable) {
        variableBuilder.repeatable();
      }

      readCustomAttributes(variableRow, headerMapVariables, attributeNamesVariables, variableBuilder);

      readCategories(name, variableBuilder);

      addVariableValueSource(new ExcelVariableValueSource(variableBuilder.build()));
    }

  }

  @Override
  public ExcelDatasource getDatasource() {
    return (ExcelDatasource) super.getDatasource();
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
        readCustomAttributes(categoryRow, headerMapCategories, attributeNamesCategories, categoryBuilder);
        variableBuilder.addCategory(((Category.Builder) categoryBuilder).build());
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
  private void readCustomAttributes(Row attributesRow, Map<String, Integer> headerMap, Set<String> attributeNames, AttributeAwareBuilder<?> builder) {
    String attributeValue;
    Locale attributeLocale;
    for(String attributeName : attributeNames) {
      attributeValue = getCellValueAsString(attributesRow.getCell(headerMap.get(attributeName)));
      attributeLocale = ExcelDatasource.getAttributeLocale(attributeName);
      if(attributeLocale != null) {
        builder.addAttribute(ExcelDatasource.getAttributeShortName(attributeName), attributeValue, attributeLocale);
      } else {
        builder.addAttribute(ExcelDatasource.getAttributeShortName(attributeName), attributeValue);
      }
    }
  }

  private String getCellValueAsString(Cell cell) {
    return ExcelDatasource.getCellValueAsString(cell);
  }

  @Override
  public void dispose() {
    // TODO Auto-generated method stub
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    throw new UnsupportedOperationException("getValueSet not supported");
  }

}

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
import org.obiba.magma.Variable.Builder;
import org.obiba.magma.support.AbstractValueTable;

class ExcelValueTable extends AbstractValueTable implements Initialisable, Disposable {

  private Sheet excelSheet;

  public ExcelValueTable(String name, ExcelDatasource datasource, Sheet excelSheet) {
    super(datasource, name);
    this.excelSheet = excelSheet;
  }

  public ExcelValueTable(String name, ExcelDatasource datasource, Sheet excelSheet, String entityType) {
    super(datasource, name);
    this.excelSheet = excelSheet;
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
    System.console().printf("Table (name = %s)\n", getName());
    Iterable<Variable> variables = getVariables();
    for(Variable variable : variables) {
      System.console().printf("  Variable (name = %s)\n", variable.getName());
      Set<Category> categories = variable.getCategories();
      for(Category category : categories) {
        System.console().printf("    Category (name = %s)\n", category.getName());
      }
    }
  }

  @Override
  public ExcelDatasource getDatasource() {
    return (ExcelDatasource) super.getDatasource();
  }

  private void readVariables() throws FileNotFoundException, IOException {

    Sheet variablesSheet = ((ExcelDatasource) getDatasource()).getVariablesSheet();
    Sheet categoriesSheet = ((ExcelDatasource) getDatasource()).getCategoriesSheet();

    Row headerVariables = variablesSheet.getRow(0);
    Map<String, Integer> headerMapVariables = ExcelDatasource.mapHeader(headerVariables);
    Set<String> attributeNamesVariables = ExcelDatasource.getAttributeNames(headerVariables, ExcelDatasource.variablesReservedAttributeNames);

    Row headerCategories = categoriesSheet.getRow(0);
    Map<String, Integer> headerMapCategories = ExcelDatasource.mapHeader(headerCategories);
    Set<String> attributeNamesCategories = ExcelDatasource.getAttributeNames(headerCategories, ExcelDatasource.categoriesReservedAttributeNames);

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
      repeatable = Boolean.valueOf(getCellValueAsString(variableRow.getCell(headerMapVariables.get("repeatable"))));

      if(repeatable) {
        variableBuilder.repeatable();
      }

      readCustomAttributes(variableRow, headerMapVariables, attributeNamesVariables, variableBuilder);

      String categoryTable;
      String categoryVariable;
      String categoryName;
      String categoryCode;
      Row categoryRow;

      int categoryRowCount = categoriesSheet.getPhysicalNumberOfRows();
      for(int x = 1; x < categoryRowCount; x++) {
        categoryRow = categoriesSheet.getRow(x);
        categoryTable = getCellValueAsString(categoryRow.getCell(headerMapCategories.get("table")));
        categoryVariable = getCellValueAsString(categoryRow.getCell(headerMapCategories.get("variable")));
        if(categoryTable.equals(getName()) && categoryVariable.equals(name)) {
          categoryName = getCellValueAsString(categoryRow.getCell(headerMapCategories.get("name")));
          categoryCode = getCellValueAsString(categoryRow.getCell(headerMapCategories.get("code")));

          // TODO Fix useless casting
          AttributeAwareBuilder categoryBuilder = Category.Builder.newCategory(categoryName).withCode(categoryCode);
          readCustomAttributes(categoryRow, headerMapCategories, attributeNamesCategories, categoryBuilder);
          variableBuilder.addCategory(((Category.Builder) categoryBuilder).build());

          // TODO Add "missing" category attributes

        }
      }

      addVariableValueSource(new ExcelVariableValueSource(variableBuilder.build()));
    }

  }

  /**
   * @param variableRow
   * @param headerMap
   * @param attributeNames
   * @param variableBuilder
   */
  private void readCustomAttributes(Row attributesRow, Map<String, Integer> headerMap, Set<String> attributeNames, AttributeAwareBuilder<Builder> builder) {
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

  public String getCellValueAsString(Cell cell) {
    return ExcelDatasource.getCellValueAsString(cell);
  }

  @Override
  public void dispose() {
    // TODO Auto-generated method stub
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    // TODO Auto-generated method stub
    return null;
  }

  public Sheet getExcelSheet() {
    return excelSheet;
  }

}

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
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.TextType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

class ExcelValueTable extends AbstractValueTable implements Initialisable {

  private static final Logger log = LoggerFactory.getLogger(ExcelValueTable.class);

  private Sheet valueTableSheet;

  /** Maps a variable's name to its Column index valueTableSheet */
  private final Map<String, Integer> variableColumns = Maps.newHashMap();

  /** Maps a variable's name to its Row in the variablesSheet */
  private final Map<String, Row> variableRows = Maps.newHashMap();

  /** Maps a category's name concatenated with the variable's name to its Row in the variablesSheet */
  private final Map<String, Row> categoryRows = Maps.newHashMap();

  public ExcelValueTable(ExcelDatasource excelDatasource, String name, String entityType) {
    super(excelDatasource, name);
    setVariableEntityProvider(new ExcelVariableEntityProvider(entityType));
  }

  @Override
  public void initialise() {
    super.initialise();
    try {
      readVariables();
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
    // Lookup in column cache
    Integer columnIndex = this.variableColumns.get(variable.getName());
    if(columnIndex != null) {
      return columnIndex;
    }
    Row variableNameRow = valueTableSheet.getRow(0);
    for(int i = 0; i < variableNameRow.getPhysicalNumberOfCells(); i++) {
      Cell cell = variableNameRow.getCell(i);
      if(ExcelUtil.getCellValueAsString(cell).equals(variable.getName())) {
        this.variableColumns.put(variable.getName(), i);
        return i;
      }
    }
    return -1;
  }

  int getVariableColumn(Variable variable) {
    int column = findVariableColumn(variable);
    if(column == -1) {
      // Add it
      Row variableNameRow = getValueTableSheet().getRow(0);
      Cell variableColumn = variableNameRow.createCell(variableNameRow.getPhysicalNumberOfCells(), Cell.CELL_TYPE_STRING);
      ExcelUtil.setCellValue(variableColumn, TextType.get(), variable.getName());
      variableColumn.setCellStyle(getDatasource().getExcelStyles("headerCellStyle"));
      column = variableColumn.getColumnIndex();
      this.variableColumns.put(variable.getName(), column);
    }
    return column;
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

  /**
   * Get the value sheet. Create it if necessary.
   * @return
   */
  Sheet getValueTableSheet() {
    if(valueTableSheet == null) {
      valueTableSheet = getDatasource().createSheetIfNotExist(getName());

      if(valueTableSheet.getPhysicalNumberOfRows() <= 0) {
        valueTableSheet.createRow(0);
      }

      // First column is for storing the Variable Entity identifiers
      Cell cell = valueTableSheet.getRow(0).createCell(0);
      ExcelUtil.setCellValue(cell, TextType.get(), "Entity ID");
      cell.setCellStyle(getDatasource().getExcelStyles("headerCellStyle"));
    }
    return valueTableSheet;
  }

  /**
   * Read the variables either from the Variables sheet or from sheet headers.
   * @throws FileNotFoundException
   * @throws IOException
   */
  private void readVariables() throws FileNotFoundException, IOException {
    if(isFromVariablesSheet()) {
      // read variables from Variables sheet
      readVariablesFromVariablesSheet();
    } else {
      // read variables from the sheet headers
      readVariablesFromTableSheet();
    }
  }

  /**
   * Variables are defined by column names and value type is text. First column is assumed to be participant identifier.
   */
  private void readVariablesFromTableSheet() {
    Sheet sheet = getDatasource().getSheet(getName());
    if(sheet != null) {
      Row variableNameRow = getValueTableSheet().getRow(0);
      for(int i = 1; i < variableNameRow.getPhysicalNumberOfCells(); i++) {
        // variable is just a name and with text values
        Cell cell = variableNameRow.getCell(i);
        String name = ExcelUtil.getCellValueAsString(cell);
        Variable.Builder variableBuilder = Variable.Builder.newVariable(name, TextType.get(), getEntityType());
        addVariableValueSource(new ExcelVariableValueSource(variableBuilder.build()));
      }
    }
  }

  /**
   * Variables are read from the variables sheet.
   */
  private void readVariablesFromVariablesSheet() {
    Map<String, Integer> headerMapVariables = getDatasource().getVariablesHeaderMap();

    Sheet variablesSheet = getDatasource().getVariablesSheet();

    Set<String> attributeNamesVariables = getDatasource().getVariablesCustomAttributeNames();

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

    Map<String, Integer> headerMapCategories = getDatasource().getCategoriesHeaderMap();
    if(headerMapCategories == null) return;
    Set<String> attributeNamesCategories = getDatasource().getCategoriesCustomAttributeNames();

    Sheet categoriesSheet = getDatasource().getCategoriesSheet();
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

  Map<String, Row> getVariableRows() {
    return variableRows;
  }

  private String getCellValueAsString(Cell cell) {
    return ExcelUtil.getCellValueAsString(cell);
  }

  private boolean isFromVariablesSheet() {
    Sheet varSheet = getDatasource().getVariablesSheet();
    return varSheet != null && varSheet.getPhysicalNumberOfRows() > 0;
  }

  private class ExcelVariableEntityProvider implements VariableEntityProvider {

    private String entityType;

    public ExcelVariableEntityProvider(String entityType) {
      if(entityType == null || entityType.trim().length() == 0) {
        this.entityType = "Participant";
      } else {
        this.entityType = entityType.trim();
      }
    }

    @Override
    public String getEntityType() {
      return entityType;
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      ImmutableSet.Builder<VariableEntity> entitiesBuilder = ImmutableSet.builder();

      if(valueTableSheet != null) {
        for(int i = 1; i < valueTableSheet.getPhysicalNumberOfRows(); i++) {
          Cell cell = valueTableSheet.getRow(i).getCell(0);
          entitiesBuilder.add(new VariableEntityBean(entityType, cell.getStringCellValue()));
        }
      }

      return entitiesBuilder.build();
    }

    @Override
    public boolean isForEntityType(String entityType) {
      return getEntityType().equals(entityType);
    }

  }

  @Override
  public Timestamps getTimestamps(ValueSet valueSet) {
    return getDatasource().getTimestamps();
  }
}

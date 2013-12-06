package org.obiba.magma.datasource.excel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.excel.support.ExcelDatasourceParsingException;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.datasource.excel.support.VariableConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("OverlyCoupledClass")
public class ExcelValueTable extends AbstractValueTable implements Initialisable {

  private Sheet valueTableSheet;

  /**
   * Maps a variable's name to its Column index valueTableSheet
   */
  private final Map<String, Integer> variableColumns = Maps.newHashMap();

  /**
   * Maps a variable's name to its list of categories (row indices)
   */
  private final Map<String, List<Integer>> variableCategoryRows = Maps.newHashMap();

  private final VariableConverter converter;

  public ExcelValueTable(Datasource excelDatasource, String name, String entityType) {
    super(excelDatasource, name);
    setVariableEntityProvider(new ExcelVariableEntityProvider(entityType));
    converter = new VariableConverter(this);
  }

  @Override
  public void initialise() {
    super.initialise();
    try {
      initVariableCategoryRows();
      readVariables();
    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Nonnull
  @Override
  public ExcelDatasource getDatasource() {
    return (ExcelDatasource) super.getDatasource();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    throw new UnsupportedOperationException("getValueSet not supported");
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    throw new UnsupportedOperationException("getValueSetTimestamps not supported");
  }

  public VariableConverter getVariableConverter() {
    return converter;
  }

  int findVariableColumn(Variable variable) {
    // Lookup in column cache
    Integer columnIndex = variableColumns.get(variable.getName());
    if(columnIndex != null) {
      return columnIndex;
    }
    Row variableNameRow = getValueTableSheet().getRow(0);
    for(int i = 0; i < variableNameRow.getPhysicalNumberOfCells(); i++) {
      Cell cell = variableNameRow.getCell(i);
      if(ExcelUtil.getCellValueAsString(cell).equals(variable.getName())) {
        variableColumns.put(variable.getName(), i);
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
      Cell variableColumn = variableNameRow
          .createCell(variableNameRow.getPhysicalNumberOfCells(), Cell.CELL_TYPE_STRING);
      ExcelUtil.setCellValue(variableColumn, TextType.get(), variable.getName());
      variableColumn.setCellStyle(getDatasource().getHeaderCellStyle());
      column = variableColumn.getColumnIndex();
      variableColumns.put(variable.getName(), column);
    }
    return column;
  }

  /**
   * Get the value sheet. Create it if necessary.
   *
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
      cell.setCellStyle(getDatasource().getHeaderCellStyle());
    }
    return valueTableSheet;
  }

  /**
   * Read the variables either from the Variables sheet or from sheet headers.
   *
   * @throws FileNotFoundException
   * @throws IOException
   */
  private void readVariables() {
    Collection<String> variableNames = new ArrayList<>();
    Collection<ExcelDatasourceParsingException> errors = new ArrayList<>();

    if(hasVariablesSheet()) {
      try {
        // read variables from Variables sheet
        readVariablesFromVariablesSheet(variableNames);
      } catch(ExcelDatasourceParsingException pe) {
        errors.add(pe);
      }
    }
    try {
      // read other variables from the sheet headers
      readVariablesFromTableSheet(variableNames);
    } catch(ExcelDatasourceParsingException pe) {
      errors.add(pe);
    }

    if(errors.size() > 0) {
      DatasourceParsingException parent = new DatasourceParsingException(
          "Errors while parsing variables of table: " + getName(), //
          "TableDefinitionErrors", getName());
      parent.setChildren(errors);
      throw parent;
    }
  }

  /**
   * Variables are defined by column names and value type is text. First column is assumed to be participant identifier.
   */
  private void readVariablesFromTableSheet(Collection<String> variableNames) {
    Sheet sheet = getDatasource().getSheet(getName());
    if(sheet == null) return;

    Collection<String> columnNames = new ArrayList<>();
    Collection<ExcelDatasourceParsingException> errors = new ArrayList<>();

    Row variableNameRow = getValueTableSheet().getRow(0);
    for(int i = 1; i < variableNameRow.getPhysicalNumberOfCells(); i++) {
      // variable is just a name and with text values
      Cell cell = variableNameRow.getCell(i);
      String name = ExcelUtil.getCellValueAsString(cell).trim();
      // required values
      if(validateVariableName(sheet, columnNames, errors, name)) {
        columnNames.add(name);
        if(!variableNames.contains(name)) {
          Variable.Builder variableBuilder = Variable.Builder.newVariable(name, TextType.get(), getEntityType());
          addVariableValueSource(new ExcelVariableValueSource(variableBuilder.build()));
        }
      }
    }

    if(errors.size() > 0) {
      ExcelDatasourceParsingException parent = new ExcelDatasourceParsingException(
          "Errors while parsing variables of table: " + getName(), //
          "TableDefinitionErrors", sheet.getSheetName(), 1, getName());
      parent.setChildren(errors);
      throw parent;
    }
  }

  private boolean validateVariableName(Sheet sheet, Collection<String> columnNames,
      Collection<ExcelDatasourceParsingException> errors, String name) {
    if(name.isEmpty()) {
      errors.add(new ExcelDatasourceParsingException("Variable name is required in table: " + getName(), //
          "VariableNameRequired", sheet.getSheetName(), 1, getName()));
      return false;
    }
    if(name.contains(":")) {
      errors.add(new ExcelDatasourceParsingException(
          "Variable name cannot contain ':' in variable: " + getName() + " / " + name, //
          "VariableNameCannotContainColon", sheet.getSheetName(), 1, getName(), name));
      return false;
    }
    if(columnNames.contains(name)) {
      errors.add(new ExcelDatasourceParsingException("Duplicate columns '" + name + "' for table: " + getName(), //
          "DuplicateColumns", sheet.getSheetName(), 1, getName(), name));
      return false;
    }
    return true;
  }

  /**
   * Variables are read from the variables sheet.
   */
  private void readVariablesFromVariablesSheet(Collection<String> variableNames) {
    if(!hasVariablesSheet()) return;

    Collection<ExcelDatasourceParsingException> errors = new ArrayList<>();
    Row firstRow = parseVariableNames(variableNames, errors);

    // check that all categories for this table has a variable definition
    parseCategoryNames(variableNames, errors);

    if(errors.size() > 0) {
      ExcelDatasourceParsingException parent = new ExcelDatasourceParsingException(
          "Errors while parsing variables of table: " + getName(), "TableDefinitionErrors",
          ExcelDatasource.VARIABLES_SHEET, firstRow == null ? -1 : firstRow.getRowNum() + 1, getName());
      parent.setChildren(errors);
      throw parent;
    }
  }

  private void parseCategoryNames(Collection<String> variableNames,
      Collection<ExcelDatasourceParsingException> errors) {
    Sheet categoriesSheet = getDatasource().getCategoriesSheet();
    int categoryRowCount = categoriesSheet.getPhysicalNumberOfRows();
    for(int i = 1; i < categoryRowCount; i++) {
      Row categoryRow = categoriesSheet.getRow(i);
      String variableName = converter.getCategoryVariableName(categoryRow);
      if(converter.getCategoryTableName(categoryRow).equals(getName())) {
        if(variableName.isEmpty()) {
          errors.add(new ExcelDatasourceParsingException("Unidentified variable for a category",
              "CategoryVariableNameRequired", ExcelDatasource.CATEGORIES_SHEET, categoryRow.getRowNum() + 1,
              getName()));
        } else if(!variableNames.contains(variableName)) {
          errors.add(new ExcelDatasourceParsingException("Unidentified variable name: " + variableName,
              "UnidentifiedVariableName", ExcelDatasource.CATEGORIES_SHEET, categoryRow.getRowNum() + 1, getName(),
              variableName));
        }
      }
    }
  }

  @Nullable
  private Row parseVariableNames(Collection<String> variableNames, Collection<ExcelDatasourceParsingException> errors) {

    Sheet variablesSheet = getDatasource().getVariablesSheet();
    int variableRowCount = variablesSheet.getPhysicalNumberOfRows();

    Row firstRow = null;
    for(int i = 1; i < variableRowCount; i++) {
      Row variableRow = variablesSheet.getRow(i);
      if(converter.isVariableRow(variableRow)) {
        if(firstRow == null) firstRow = variableRow;
        String name = converter.getVariableName(variableRow);

        if(variableNames.contains(name)) {
          // do not parse duplicates
          errors.add(new ExcelDatasourceParsingException("Duplicate variable name", //
              "DuplicateVariableName", ExcelDatasource.VARIABLES_SHEET, variableRow.getRowNum() + 1, getName(), name));
        } else {
          variableNames.add(name);
          addParsedVariable(errors, variableRow);
        }
      }
    }
    return firstRow;
  }

  private void addParsedVariable(Collection<ExcelDatasourceParsingException> errors, Row variableRow) {
    try {
      Variable variable = converter.unmarshall(variableRow);
      addVariableValueSource(new ExcelVariableValueSource(variable));
    } catch(ExcelDatasourceParsingException pe) {
      errors.add(pe);
    } catch(Exception e) {
      errors.add(new ExcelDatasourceParsingException("Unexpected error in variable: " + e.getMessage(), e, //
          "UnexpectedErrorInVariable", ExcelDatasource.VARIABLES_SHEET, variableRow.getRowNum() + 1, getName()));
    }
  }

  public List<Integer> getVariableCategoryRows(String variableName) {
    List<Integer> rows = variableCategoryRows.get(getName() + "." + variableName);
    return rows == null ? new ArrayList<Integer>() : rows;
  }

  private void initVariableCategoryRows() {
    Sheet categoriesSheet = getDatasource().getCategoriesSheet();
    int categoryRowCount = categoriesSheet.getPhysicalNumberOfRows();

    for(int rowIndex = 1; rowIndex < categoryRowCount; rowIndex++) {
      Row categoryRow = categoriesSheet.getRow(rowIndex);
      if(converter.getCategoryTableName(categoryRow).equals(getName())) {
        String variableName = converter.getCategoryVariableName(categoryRow);
        if(variableName.length() != 0) {
          List<Integer> categoryRows = variableCategoryRows.get(getName() + "." + variableName);
          if(categoryRows == null) {
            categoryRows = Lists.newArrayList();
            variableCategoryRows.put(getName() + "." + variableName, categoryRows);
          }
          categoryRows.add(rowIndex);
        }
      }
    }
  }

  private boolean hasVariablesSheet() {
    return getDatasource().hasVariablesSheet() && getDatasource().getVariablesSheet().getPhysicalNumberOfRows() > 0;
  }

  private class ExcelVariableEntityProvider implements VariableEntityProvider {

    private final String entityType;

    private ExcelVariableEntityProvider(String entityType) {
      this.entityType = entityType == null || entityType.trim().isEmpty() ? "Participant" : entityType.trim();
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
          entitiesBuilder.add(new VariableEntityBean(entityType, ExcelUtil.getCellValueAsString(cell)));
        }
      }

      return entitiesBuilder.build();
    }

    @Override
    public boolean isForEntityType(String type) {
      return getEntityType().equals(type);
    }

  }

  @Nonnull
  @Override
  public Timestamps getTimestamps() {
    return getDatasource().getTimestamps();
  }

}

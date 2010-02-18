package org.obiba.magma.datasource.excel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
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

  private void readVariables() throws FileNotFoundException, IOException {

    Sheet variableSheet = ((ExcelDatasource) getDatasource()).getWorkbook().getSheet("Variables");
    int rowCount = variableSheet.getPhysicalNumberOfRows();
    Row header = variableSheet.getRow(0);

    Row variableRow;
    Map<String, Integer> headerMap = ExcelDatasource.mapHeader(header);

    Set<String> attributeNames = ExcelDatasource.getAttributeNames(header);
    String name;
    String valueType;
    String entityType;
    String mimeType;
    String unit;
    String occurrenceGroup;
    Boolean repeatable;

    for(int i = 1; i < rowCount; i++) {
      variableRow = variableSheet.getRow(i);

      name = getCellValueAsString(variableRow.getCell(headerMap.get("name")));
      valueType = getCellValueAsString(variableRow.getCell(headerMap.get("valueType")));
      entityType = getCellValueAsString(variableRow.getCell(headerMap.get("entityType")));
      mimeType = getCellValueAsString(variableRow.getCell(headerMap.get("mimeType")));
      unit = getCellValueAsString(variableRow.getCell(headerMap.get("unit")));
      occurrenceGroup = getCellValueAsString(variableRow.getCell(headerMap.get("occurrenceGroup")));

      Variable.Builder variableBuilder = Variable.Builder.newVariable(name, ValueType.Factory.forName(valueType), entityType).mimeType(mimeType).unit(unit).occurrenceGroup(occurrenceGroup);
      repeatable = Boolean.valueOf(getCellValueAsString(variableRow.getCell(headerMap.get("repeatable"))));

      if(repeatable) {
        variableBuilder.repeatable();
      }

      String attributeValue;
      Locale attributeLocale;
      for(String attributeName : attributeNames) {
        attributeValue = getCellValueAsString(variableRow.getCell(headerMap.get(attributeName)));
        attributeLocale = ExcelDatasource.getAttributeLocale(attributeName);
        if(attributeLocale != null) {
          variableBuilder.addAttribute(ExcelDatasource.getAttributeShortName(attributeName), attributeValue, attributeLocale);
        } else {
          variableBuilder.addAttribute(ExcelDatasource.getAttributeShortName(attributeName), attributeValue);
        }

        addVariableValueSource(new ExcelVariableValueSource(variableBuilder.build()));

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

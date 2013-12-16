package org.obiba.magma.datasource.excel;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.datasource.excel.support.VariableConverter;
import org.obiba.magma.type.TextType;

public class ExcelValueTableWriter implements ValueTableWriter {

  private final ExcelValueTable valueTable;

  public ExcelValueTableWriter(ExcelValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public VariableWriter writeVariables() {
    return new ExcelVariableWriter();
  }

  @NotNull
  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    return new ExcelValueSetWriter(entity);
  }

  @Override
  public void close() throws IOException {
  }

  private class ExcelVariableWriter implements VariableWriter {

    private ExcelVariableWriter() {
    }

    private Sheet getVariablesSheet() {
      return valueTable.getDatasource().getVariablesSheet();
    }

    private Sheet getCategoriesSheet() {
      return valueTable.getDatasource().getCategoriesSheet();
    }

    @Override
    public void writeVariable(@NotNull Variable variable) {
      VariableConverter converter = valueTable.getVariableConverter();

      // prepare the header rows
      Row headerRowVariables = getVariablesSheet().getRow(0);
      if(headerRowVariables == null) {
        headerRowVariables = getVariablesSheet().createRow(0);
      }
      updateVariableSheetHeaderRow(headerRowVariables);

      Row headerRowCategories = getCategoriesSheet().getRow(0);
      if(headerRowCategories == null) {
        headerRowCategories = getCategoriesSheet().createRow(0);
      }
      updateCategorySheetHeaderRow(headerRowCategories);

      converter.marshall(variable, headerRowVariables, headerRowCategories);
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      throw new UnsupportedOperationException("Variable cannot be removed from a Excel file");
    }

    private void updateVariableSheetHeaderRow(Row headerRow) {
      VariableConverter converter = valueTable.getVariableConverter();
      for(String reservedAttributeName : VariableConverter.reservedVariableHeaders) {
        if(converter.getVariableHeaderIndex(reservedAttributeName) == null) {
          converter.putVariableHeaderIndex(reservedAttributeName, getLastCellNum(headerRow));
          createHeaderCell(headerRow, reservedAttributeName);
        }
      }
    }

    private void updateCategorySheetHeaderRow(Row headerRow) {
      VariableConverter converter = valueTable.getVariableConverter();
      for(String reservedAttributeName : VariableConverter.reservedCategoryHeaders) {
        if(converter.getCategoryHeaderIndex(reservedAttributeName) == null) {
          converter.putCategoryHeaderIndex(reservedAttributeName, getLastCellNum(headerRow));
          createHeaderCell(headerRow, reservedAttributeName);
        }
      }
    }

    private void createHeaderCell(Row headerRow, String header) {
      Cell headerCell = headerRow.createCell(getLastCellNum(headerRow));
      headerCell.setCellValue(header);
      headerCell.setCellStyle(valueTable.getDatasource().getHeaderCellStyle());
    }

    private int getLastCellNum(Row row) {
      return row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
    }

    @Override
    public void close() throws IOException {
    }

  }

  private class ExcelValueSetWriter implements ValueSetWriter {

    private final Row entityRow;

    private ExcelValueSetWriter(VariableEntity entity) {
      Sheet tableSheet = valueTable.getValueTableSheet();
      entityRow = tableSheet.createRow(tableSheet.getPhysicalNumberOfRows());
      ExcelUtil.setCellValue(entityRow.createCell(0), TextType.get(), entity.getIdentifier());
    }

    @Override
    public void writeValue(@NotNull Variable variable, Value value) {
      // Will create the column if it doesn't exist.
      int variableColumn = valueTable.getVariableColumn(variable);
      ExcelUtil.setCellValue(entityRow.createCell(variableColumn), value);
    }

    @Override
    public void close() throws IOException {
    }

  }

}

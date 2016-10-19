/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.obiba.magma.Attribute;
import org.obiba.magma.Disposable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.datasource.excel.support.VariableConverter;
import org.obiba.magma.type.TextType;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelValueTableWriter implements ValueTableWriter {

  private final ExcelValueTable valueTable;
  private ExcelVariableWriter excelVariableWriter;
  private List<VariableWithMetadata> variablesWithMetadata = new ArrayList<>();

  public ExcelValueTableWriter(ExcelValueTable valueTable, ExcelValueTableWriter excelValueTableWriter) {
    this(valueTable);
    this.variablesWithMetadata = excelValueTableWriter.variablesWithMetadata;
  }

  public ExcelValueTableWriter(ExcelValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public ExcelVariableWriter writeVariables() {
    if (excelVariableWriter == null) {
      excelVariableWriter = new ExcelVariableWriter();
    }
    return excelVariableWriter;
  }

  @NotNull
  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    return new ExcelValueSetWriter(entity);
  }

  @Override
  public void close() {
  }

  private class ExcelVariableWriter implements VariableWriter, Disposable {

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

      variablesWithMetadata.add(new VariableWithMetadata(variable, headerRowVariables, headerRowCategories, valueTable.getName()));
    }

    @Override
    public void dispose() {

      VariableConverter converter = valueTable.getVariableConverter();

      List<Attribute> variablesAttributes = variablesWithMetadata.stream()
              .map(t -> t.getVariable().getAttributes())
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      List<Attribute> categoriesAttributes = variablesWithMetadata.stream()
              .flatMap(variableWithMetadata -> variableWithMetadata.getVariable().getCategories().stream())
              .flatMap(attributes -> attributes.getAttributes().stream())
              .collect(Collectors.toList());

      converter.createVariablesHeaders(variablesAttributes, getVariablesSheet().getRow(0));
      converter.createCategoriesHeaders(categoriesAttributes, getCategoriesSheet().getRow(0));

      for (VariableWithMetadata variableWithMetadata : variablesWithMetadata) {
        converter.marshall(variableWithMetadata);
      }
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
    public void close() {
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
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

  }

  public class VariableWithMetadata {

    private Variable variable;
    private Row headerRowVariables;
    private Row headerRowCategories;
    private String tableName;

    VariableWithMetadata(Variable variable, Row headerRowVariables, Row headerRowCategories, String tableName) {
      this.variable = variable;
      this.headerRowVariables = headerRowVariables;
      this.headerRowCategories = headerRowCategories;
      this.tableName = tableName;
    }

    public Variable getVariable() {
      return variable;
    }

    public Row getHeaderRowVariables() {
      return headerRowVariables;
    }

    public Row getHeaderRowCategories() {
      return headerRowCategories;
    }

    public String getTableName() {
      return tableName;
    }
  }
}

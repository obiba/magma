package org.obiba.magma.datasource.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Implements a {@code Datasource} on top of an Excel Workbook.
 */
public class ExcelDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(ExcelDatasource.class);

  public static final List<String> variablesReservedAttributeNames = Lists.newArrayList("table", "name", "valueType", "entityType", "mimeType", "unit", "occurrenceGroup", "repeatable");

  public static final List<String> categoriesReservedAttributeNames = Lists.newArrayList("table", "variable", "name", "code", "missing");

  public static final Set<String> sheetReservedNames = Sets.newHashSet(new String[] { "Variables", "Categories" });

  private FileInputStream excelInputStream;

  private Workbook excelWorkbook;

  private Sheet variablesSheet;

  private Sheet categorySheet;

  private File excelFile;

  private Map<String, CellStyle> excelStyles;

  public ExcelDatasource(String name, File excelFile) {
    super(name, "excel");
    this.excelFile = excelFile;
  }

  @Override
  protected void onInitialise() {

    if(excelFile.exists()) {
      try {
        excelWorkbook = new HSSFWorkbook(excelInputStream = new FileInputStream(excelFile));
      } catch(Exception e) {
        throw new RuntimeException("Could not access the file : " + excelFile, e);
      }
    } else {
      excelWorkbook = new HSSFWorkbook();
    }

    variablesSheet = createSheetIfNotExist("Variables");
    categorySheet = createSheetIfNotExist("Categories");

    createExcelStyles();

  }

  private void createExcelStyles() {
    excelStyles = new HashMap<String, CellStyle>();

    CellStyle headerCellStyle = variablesSheet.getWorkbook().createCellStyle();
    Font headerFont = excelWorkbook.createFont();
    headerFont.setBoldweight((short) 700);
    headerCellStyle.setFont(headerFont);

    excelStyles.put("headerCellStyle", headerCellStyle);

  }

  public CellStyle getExcelStyle(String styleName) {
    return excelStyles.get(styleName);
  }

  private Sheet createSheetIfNotExist(String tableName) {
    Sheet sheet;
    if((sheet = excelWorkbook.getSheet(tableName)) == null) {
      sheet = excelWorkbook.createSheet(tableName);
    }
    return sheet;
  }

  @Override
  protected void onDispose() {

    if(excelInputStream != null) {
      try {
        excelInputStream.close();
      } catch(Exception couldNotCloseStream) {
        log.warn("Could not close the excelInputStream", couldNotCloseStream);
      }
    }

    // Write the workbook (datasource) to file.
    FileOutputStream excelOutputStream = null;
    try {
      excelOutputStream = new FileOutputStream(excelFile);
      excelWorkbook.write(excelOutputStream);
    } catch(Exception couldNotWriteToStream) {
      throw new RuntimeException("Could not write to excelOutputStream", couldNotWriteToStream);
    } finally {
      try {
        excelOutputStream.close();
      } catch(IOException couldNotCloseStream) {
        log.warn("Could not close the excelOutputStream", couldNotCloseStream);
      }
    }

  }

  @Override
  protected Set<String> getValueTableNames() {
    int sheetCount = excelWorkbook.getNumberOfSheets();
    Set<String> tableNames = new HashSet<String>();
    String tableName;
    for(int i = 0; i < sheetCount; i++) {
      tableName = excelWorkbook.getSheetAt(i).getSheetName();
      if(!sheetReservedNames.contains(tableName)) {
        tableNames.add(tableName);
      }
    }
    return tableNames;
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new ExcelValueTable(tableName, this, createSheetIfNotExist(tableName));
  }

  public ValueTableWriter createWriter(String name, String entityType) {
    ExcelValueTable valueTable = null;
    if(hasValueTable(name)) {
      valueTable = (ExcelValueTable) getValueTable(name);
    } else {
      addValueTable(valueTable = new ExcelValueTable(name, this, createSheetIfNotExist(name), entityType));
    }
    return new ExcelValueTableWriter(valueTable);
  }

  public static String getAttributeShortName(String attributeName) {
    return attributeName.split(":")[0];
  }

  public static Locale getAttributeLocale(String attributeName) {
    String[] parsedAttributeName = attributeName.split(":");
    if(parsedAttributeName.length > 1) {
      return new Locale(parsedAttributeName[1]);
    }
    return null;
  }

  public static Set<String> getAttributeNames(Row rowHeader, List<String> reservedNames) {
    Set<String> attributesNames = new HashSet<String>();
    int cellCount = rowHeader.getPhysicalNumberOfCells();
    for(int i = 0; i < cellCount; i++) {
      if(!reservedNames.contains(attributesNames)) {
        attributesNames.add(getCellValueAsString(rowHeader.getCell(i)));
      }
    }
    return attributesNames;
  }

  public static Map<String, Integer> mapHeader(Row rowHeader) {
    Map<String, Integer> headerMap = new HashMap<String, Integer>();
    int cellCount = rowHeader.getPhysicalNumberOfCells();
    Cell cell;

    for(int i = 0; i < cellCount; i++) {
      cell = rowHeader.getCell(i);
      headerMap.put(cell.getStringCellValue(), i);
    }
    return headerMap;
  }

  public static String getCellValueAsString(Cell cell) {
    String value = "";
    if(cell != null) {
      switch(cell.getCellType()) {
      case Cell.CELL_TYPE_STRING:
      case Cell.CELL_TYPE_BLANK:
        value = cell.getStringCellValue();
        break;
      case Cell.CELL_TYPE_BOOLEAN:
        value = String.valueOf(cell.getBooleanCellValue());
        break;
      case Cell.CELL_TYPE_ERROR:
        value = String.valueOf(cell.getErrorCellValue());
        break;
      case Cell.CELL_TYPE_FORMULA:
        value = String.valueOf(cell.getCellFormula());
        break;
      case Cell.CELL_TYPE_NUMERIC:
        value = String.valueOf(cell.getNumericCellValue());
        break;
      default:
        break;
      }
    }
    return value;
  }

  public Workbook getWorkbook() {
    return excelWorkbook;
  }

  public Sheet getVariablesSheet() {
    return variablesSheet;
  }

  public Sheet getCategoriesSheet() {
    return categorySheet;
  }
}

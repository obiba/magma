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
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
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

  public static final List<String> attributesReservedAttributeNames = Lists.newArrayList("table", "attributeAwareType", "attributeAware", "attribute", "valueType");

  public static final Set<String> sheetReservedNames = Sets.newHashSet(new String[] { "Variables", "Categories", "Attributes" });

  private Workbook excelWorkbook;

  private Sheet variablesSheet;

  private Sheet categoriesSheet;

  private Sheet attributesSheet;

  private File excelFile;

  private Map<String, CellStyle> excelStyles;

  public ExcelDatasource(String name, File excelFile) {
    super(name, "excel");
    this.excelFile = excelFile;
  }

  public ValueTableWriter createWriter(String name, String entityType) {
    ExcelValueTable valueTable = null;
    if(hasValueTable(name)) {
      valueTable = (ExcelValueTable) getValueTable(name);
    } else {
      addValueTable(valueTable = new ExcelValueTable(this, name, createSheetIfNotExist(name), entityType));
    }
    return new ExcelValueTableWriter(valueTable);
  }

  @Override
  protected void onInitialise() {
    if(excelFile.exists()) {
      try {
        // HSSFWorkbook constructor will close the stream by itself
        excelWorkbook = new HSSFWorkbook(new FileInputStream(excelFile));
      } catch(IOException e) {
        throw new MagmaRuntimeException("Exception reading excel spreadsheet " + excelFile.getName(), e);
      }
    } else {
      excelWorkbook = new HSSFWorkbook();
    }

    variablesSheet = createSheetIfNotExist("Variables");
    categoriesSheet = createSheetIfNotExist("Categories");
    // OPAL-173: Commented out to remove the attributes sheet
    // attributesSheet = createSheetIfNotExist("Attributes");

    createExcelStyles();
  }

  @Override
  protected void onDispose() {
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
    return new ExcelValueTable(this, tableName, createSheetIfNotExist(tableName), "");
  }

  Workbook getWorkbook() {
    return excelWorkbook;
  }

  Sheet getVariablesSheet() {
    return variablesSheet;
  }

  Sheet getCategoriesSheet() {
    return categoriesSheet;
  }

  Sheet getAttributesSheet() {
    log.error("OPAL-173: attributesSheet has been removed. The method will return null which may cause a NPE.");
    return attributesSheet;
  }

  CellStyle getExcelStyles(String styleName) {
    return excelStyles.get(styleName);
  }

  static String getAttributeShortName(String attributeName) {
    return attributeName.split(":")[0];
  }

  static Locale getAttributeLocale(String attributeName) {
    String[] parsedAttributeName = attributeName.split(":");
    if(parsedAttributeName.length > 1) {
      return new Locale(parsedAttributeName[1]);
    }
    return null;
  }

  static Set<String> getCustomAttributeNames(Row rowHeader, List<String> reservedAttributeNames) {
    Set<String> attributesNames = new HashSet<String>();
    int cellCount = rowHeader.getPhysicalNumberOfCells();
    for(int i = 0; i < cellCount; i++) {
      String attributeName = ExcelUtil.getCellValueAsString(rowHeader.getCell(i));
      if(!reservedAttributeNames.contains(attributeName)) {
        attributesNames.add(attributeName);
      }
    }
    return attributesNames;
  }

  static Map<String, Integer> mapSheetHeader(Row rowHeader) {
    Map<String, Integer> headerMap = new HashMap<String, Integer>();
    int cellCount = rowHeader.getPhysicalNumberOfCells();
    Cell cell;

    for(int i = 0; i < cellCount; i++) {
      cell = rowHeader.getCell(i);
      headerMap.put(cell.getStringCellValue(), i);
    }
    return headerMap;
  }

  private Sheet createSheetIfNotExist(String tableName) {
    Sheet sheet;
    if((sheet = excelWorkbook.getSheet(tableName)) == null) {
      sheet = excelWorkbook.createSheet(tableName);
    }
    return sheet;
  }

  private void createExcelStyles() {
    excelStyles = new HashMap<String, CellStyle>();

    CellStyle headerCellStyle = variablesSheet.getWorkbook().createCellStyle();
    Font headerFont = excelWorkbook.createFont();
    headerFont.setBoldweight((short) 700);
    headerCellStyle.setFont(headerFont);

    excelStyles.put("headerCellStyle", headerCellStyle);
  }
}

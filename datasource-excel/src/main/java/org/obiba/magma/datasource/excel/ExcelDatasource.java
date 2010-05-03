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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlOptions;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.datasource.excel.support.NameConverter;
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
    if(hasValueTable(NameConverter.toExcelName(name))) {
      valueTable = (ExcelValueTable) getValueTable(NameConverter.toExcelName(name));
    } else {
      addValueTable(valueTable = new ExcelValueTable(this, name, createSheetIfNotExist(name), entityType));
    }
    return new ExcelValueTableWriter(valueTable);
  }

  @Override
  protected void onInitialise() {
    if(excelFile.exists()) {
      try {
        // WorkbookFactory will close the stream by itself
        // This will create the proper type of Workbook (HSSF vs. XSSF)
        excelWorkbook = WorkbookFactory.create(new FileInputStream(excelFile));
      } catch(IOException e) {
        throw new MagmaRuntimeException("Exception reading excel spreadsheet " + excelFile.getName(), e);
      } catch(InvalidFormatException e) {
        throw new MagmaRuntimeException("Invalid excel spreadsheet format " + excelFile.getName(), e);
      }
    } else {
      if(excelFile.getName().endsWith("xls")) {
        // Excel 97 format. Supports up to 256 columns only.
        log.warn("Creating an ExcelDatasource using Excel 97 format which only supports 256 columns. This may not be sufficient for large amounts of variables. Specify a filename with an extension other than 'xls' to use Excel 2007 format.");
        excelWorkbook = new HSSFWorkbook();
      } else {
        // Create a XSSFWorkbook to support more than 256 columns and 64K rows.
        excelWorkbook = new XSSFWorkbook();
      }
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
      // Fix for OPAL-238 using POI 3.6
      // The following lines can be removed when using a POI version that has this internal fix.
      // See https://issues.apache.org/bugzilla/show_bug.cgi?id=48936
      // ---
      XmlOptions options = POIXMLDocumentPart.DEFAULT_XML_OPTIONS;
      options.setSaveCDataLengthThreshold(1000000);
      options.setSaveCDataEntityCountThreshold(-1);
      // ---

      excelOutputStream = new FileOutputStream(excelFile);
      excelWorkbook.write(excelOutputStream);
    } catch(Exception e) {
      throw new MagmaRuntimeException("Could not write to excelOutputStream", e);
    } finally {
      try {
        if(excelOutputStream != null) excelOutputStream.close();
      } catch(IOException e) {
        log.warn("Could not close the excelOutputStream", e);
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
    String sheetName = NameConverter.toExcelName(tableName);
    sheet = excelWorkbook.getSheet(sheetName);
    if(sheet == null) {
      sheet = excelWorkbook.createSheet(sheetName);
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

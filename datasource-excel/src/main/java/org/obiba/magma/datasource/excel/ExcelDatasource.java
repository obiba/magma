package org.obiba.magma.datasource.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.obiba.magma.Timestamps;
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

  public static final Set<String> sheetReservedNames = Sets.newHashSet(new String[] { "Variables", "Categories", "Help" });

  private Workbook excelWorkbook;

  private Sheet variablesSheet;

  private Sheet categoriesSheet;

  private File excelFile;

  private OutputStream excelOutput;

  private InputStream excelInput;

  private Map<String, CellStyle> excelStyles;

  private Map<String, ExcelValueTable> valueTablesMapOnInit = new HashMap<String, ExcelValueTable>();

  /**
   * Excel workbook will be read from the provided file if it exists, and will be written in the file at datasource
   * disposal.
   * @param name
   * @param excelFile
   */
  public ExcelDatasource(String name, File excelFile) {
    super(name, "excel");
    this.excelFile = excelFile;
  }

  /**
   * Excel workbook will be written in the output stream at datasource disposal.
   * @param name
   * @param output
   */
  public ExcelDatasource(String name, OutputStream output) {
    super(name, "excel");
    this.excelOutput = output;
  }

  /**
   * Excel workbook will be read from input stream.
   * @param name
   * @param input
   */
  public ExcelDatasource(String name, InputStream input) {
    super(name, "excel");
    this.excelInput = input;
  }

  /**
   * Set the output stream to which the Excel workbook will be persisted at datasource disposal.
   * @param excelOutput
   */
  public void setExcelOutput(OutputStream excelOutput) {
    this.excelOutput = excelOutput;
  }

  public ValueTableWriter createWriter(String name, String entityType) {
    ExcelValueTable valueTable = null;
    if(hasValueTable(name)) {
      valueTable = (ExcelValueTable) getValueTable(name);
    } else {
      addValueTable(valueTable = new ExcelValueTable(this, name, entityType));
    }
    return new ExcelValueTableWriter(valueTable);
  }

  @Override
  protected void onInitialise() {
    if(excelFile != null) {
      createWorbookFromFile();
    } else if(excelInput != null) {
      createWorkbookFromInputStream();
    } else {
      // Create a XSSFWorkbook that will be written in output stream
      excelWorkbook = new XSSFWorkbook();
    }

    variablesSheet = createSheetIfNotExist("Variables");
    categoriesSheet = createSheetIfNotExist("Categories");

    createExcelStyles();
  }

  private void createWorbookFromFile() {
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
  }

  private void createWorkbookFromInputStream() {
    try {
      excelWorkbook = WorkbookFactory.create(excelInput);
    } catch(InvalidFormatException e) {
      throw new MagmaRuntimeException("Invalid excel spreadsheet format from input stream.");
    } catch(IOException e) {
      throw new MagmaRuntimeException("Exception reading excel spreadsheet from input stream.");
    }
  }

  /**
   * Write the Excel workbook into provided output stream.
   * @param excelOutputStream
   * @throws IOException
   */
  private void writeWorkbook(OutputStream excelOutputStream) throws IOException {
    // Fix for OPAL-238 using POI 3.6
    // The following lines can be removed when using a POI version that has this internal fix.
    // See https://issues.apache.org/bugzilla/show_bug.cgi?id=48936
    // ---
    XmlOptions options = POIXMLDocumentPart.DEFAULT_XML_OPTIONS;
    options.setSaveCDataLengthThreshold(1000000);
    options.setSaveCDataEntityCountThreshold(-1);

    excelWorkbook.write(excelOutputStream);
  }

  @Override
  protected void onDispose() {
    // Write the workbook (datasource) to file/outputstream if any of them is defined
    OutputStream out = null;
    try {
      if(excelFile != null) {
        out = new FileOutputStream(excelFile);
      } else {
        out = excelOutput;
      }
      if(out != null) {
        writeWorkbook(out);
      }
    } catch(Exception e) {
      throw new MagmaRuntimeException("Could not write to excel output stream", e);
    } finally {
      try {
        if(out != null) out.close();
      } catch(IOException e) {
        log.warn("Could not close the excel output stream", e);
      }
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    Set<String> sheetNames = new HashSet<String>();

    // find the table names from the Variables sheet
    Row headerVariables = variablesSheet.getRow(0);
    if(headerVariables != null) {
      Map<String, Integer> headerMapVariables = getVariablesHeaderMap();
      if(headerMapVariables != null) {
        for(int i = 1; i < variablesSheet.getPhysicalNumberOfRows(); i++) {
          Row variableRow = variablesSheet.getRow(i);
          String tableName = ExcelUtil.getCellValueAsString(variableRow.getCell(headerMapVariables.get("table")));
          if(!valueTablesMapOnInit.containsKey(tableName)) {
            String entityType = ExcelUtil.getCellValueAsString(variableRow.getCell(headerMapVariables.get("entityType")));
            valueTablesMapOnInit.put(tableName, new ExcelValueTable(this, tableName, entityType));
            sheetNames.add(getSheetName(tableName));
          }
        }
      }
    }

    // find other tables from their sheet name
    int sheetCount = excelWorkbook.getNumberOfSheets();
    for(int i = 0; i < sheetCount; i++) {
      String sheetName = excelWorkbook.getSheetAt(i).getSheetName();
      if(!sheetNames.contains(sheetName) && !sheetReservedNames.contains(sheetName)) {
        valueTablesMapOnInit.put(sheetName, new ExcelValueTable(this, sheetName, "Participant"));
      }
    }
    return valueTablesMapOnInit.keySet();
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return valueTablesMapOnInit.get(tableName);
  }

  Sheet getVariablesSheet() {
    return variablesSheet;
  }

  Sheet getCategoriesSheet() {
    return categoriesSheet;
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

  Set<String> getVariablesCustomAttributeNames() {
    return getCustomAttributeNames(variablesSheet.getRow(0), variablesReservedAttributeNames);
  }

  Set<String> getCategoriesCustomAttributeNames() {
    return getCustomAttributeNames(categoriesSheet.getRow(0), categoriesReservedAttributeNames);
  }

  private Set<String> getCustomAttributeNames(Row rowHeader, List<String> reservedAttributeNames) {
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

  Map<String, Integer> getVariablesHeaderMap() {
    return getMapSheetHeader(variablesSheet.getRow(0));
  }

  Map<String, Integer> getCategoriesHeaderMap() {
    return getMapSheetHeader(categoriesSheet.getRow(0));
  }

  private Map<String, Integer> getMapSheetHeader(Row rowHeader) {
    Map<String, Integer> headerMap = null;
    if(rowHeader != null) {
      headerMap = new HashMap<String, Integer>();
      int cellCount = rowHeader.getPhysicalNumberOfCells();
      Cell cell;

      for(int i = 0; i < cellCount; i++) {
        cell = rowHeader.getCell(i);
        headerMap.put(cell.getStringCellValue(), i);
      }
    }
    return headerMap;
  }

  Sheet createSheetIfNotExist(String tableName) {
    Sheet sheet;
    String sheetName = getSheetName(tableName);
    sheet = excelWorkbook.getSheet(sheetName);
    if(sheet == null) {
      sheet = excelWorkbook.createSheet(sheetName);
    }
    return sheet;
  }

  /**
   * Get converted sheet name from table name.
   * @param tableName
   * @return
   */
  private String getSheetName(String tableName) {
    String sheetName = NameConverter.toExcelName(tableName);
    // Excel allows a maximum of 30 chars for table names
    if(sheetName.length() > 30) {
      sheetName = sheetName.substring(0, 27) + "$" + (sheetName.length() - 30);
    }
    log.debug("{}={}", tableName, sheetName);
    return sheetName;
  }

  /**
   * Get the sheet from table name.
   * @param tableName
   * @return null if sheet does not exists
   */
  Sheet getSheet(String tableName) {
    return excelWorkbook.getSheet(getSheetName(tableName));
  }

  private void createExcelStyles() {
    excelStyles = new HashMap<String, CellStyle>();

    CellStyle headerCellStyle = variablesSheet.getWorkbook().createCellStyle();
    Font headerFont = excelWorkbook.createFont();
    headerFont.setBoldweight((short) 700);
    headerCellStyle.setFont(headerFont);

    excelStyles.put("headerCellStyle", headerCellStyle);
  }

  Timestamps getTimestamps() {
    return new ExcelTimestamps(excelFile);
  }
}

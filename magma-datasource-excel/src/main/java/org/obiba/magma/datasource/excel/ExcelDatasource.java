package org.obiba.magma.datasource.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

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
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.excel.support.ExcelDatasourceParsingException;
import org.obiba.magma.datasource.excel.support.ExcelUtil;
import org.obiba.magma.datasource.excel.support.NameConverter;
import org.obiba.magma.datasource.excel.support.VariableConverter;
import org.obiba.magma.support.AbstractDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Implements a {@code Datasource} on top of an Excel Workbook.
 */
public class ExcelDatasource extends AbstractDatasource {

  private static final Logger log = LoggerFactory.getLogger(ExcelDatasource.class);

  public static final String VARIABLES_SHEET = "Variables";

  public static final String CATEGORIES_SHEET = "Categories";

  public static final String HELP_SHEET = "Help";

  public static final String DEFAULT_TABLE_NAME = "Table";

  public static final Set<String> SHEET_RESERVED_NAMES = Sets.newHashSet(VARIABLES_SHEET, CATEGORIES_SHEET, HELP_SHEET);

  private static final int SHEET_NAME_MAX_LENGTH = 30;

  private static final int BOLD_WEIGHT = 700;

  private Workbook excelWorkbook;

  private Sheet variablesSheet;

  private Sheet categoriesSheet;

  private File excelFile;

  private OutputStream excelOutput;

  private InputStream excelInput;

  private Map<String, CellStyle> excelStyles;

  private final Map<String, ExcelValueTable> valueTablesMapOnInit = new LinkedHashMap<String, ExcelValueTable>(100);

  /**
   * Excel workbook will be read from the provided file if it exists, and will be written in the file at datasource
   * disposal.
   *
   * @param name
   * @param excelFile
   */
  public ExcelDatasource(String name, File excelFile) {
    super(name, "excel");
    this.excelFile = excelFile;
  }

  /**
   * Excel workbook will be written in the output stream at datasource disposal.
   *
   * @param name
   * @param output
   */
  public ExcelDatasource(String name, OutputStream output) {
    super(name, "excel");
    excelOutput = output;
  }

  /**
   * Excel workbook will be read from input stream.
   *
   * @param name
   * @param input
   */
  public ExcelDatasource(String name, InputStream input) {
    super(name, "excel");
    excelInput = input;
  }

  /**
   * Set the output stream to which the Excel workbook will be persisted at datasource disposal.
   *
   * @param excelOutput
   */
  public void setExcelOutput(OutputStream excelOutput) {
    this.excelOutput = excelOutput;
  }

  @Override
  @Nonnull
  public ValueTableWriter createWriter(@Nonnull String name, @Nonnull String entityType) {
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
        log.warn(
            "Creating an ExcelDatasource using Excel 97 format which only supports 256 columns. This may not be sufficient for large amounts of variables. Specify a filename with an extension other than 'xls' to use Excel 2007 format.");
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
    } catch(IllegalArgumentException e) {
      throw new MagmaRuntimeException(
          "Invalid excel spreadsheet format from input stream (neither an OLE2 stream nor an OOXML stream).");
    } catch(InvalidFormatException e) {
      throw new MagmaRuntimeException("Invalid excel spreadsheet format from input stream.");
    } catch(IOException e) {
      throw new MagmaRuntimeException("Exception reading excel spreadsheet from input stream.");
    }
  }

  /**
   * Write the Excel workbook into provided output stream.
   *
   * @param excelOutputStream
   * @throws IOException
   */
  private void writeWorkbook(OutputStream excelOutputStream) throws IOException {
    excelWorkbook.write(excelOutputStream);
  }

  @Override
  protected void onDispose() {
    // Write the workbook (datasource) to file/OutputStream if any of them is defined
    OutputStream out = null;
    try {
      out = excelFile == null ? excelOutput : new FileOutputStream(excelFile);
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
    Collection<String> sheetNames = new LinkedHashSet<String>(100);

    // find the table names from the Variables sheet
    if(hasVariablesSheet()) {
      Row headerVariables = getVariablesSheet().getRow(0);
      if(headerVariables != null) {
        Map<String, Integer> headerMapVariables = getVariablesHeaderMap();
        if(headerMapVariables != null) {
          List<ExcelDatasourceParsingException> errors = readValueTablesFromVariableSheet(headerMapVariables,
              sheetNames);
          if(errors.size() > 0) {
            ExcelDatasourceParsingException parent = new ExcelDatasourceParsingException(
                "Errors while parsing variables", //
                "TableDefinitionErrors", getVariablesSheet().getSheetName(), 1, getName());
            parent.setChildren(errors);
            throw parent;
          }
        }
      }
    }

    // find other tables from their sheet name
    int sheetCount = excelWorkbook.getNumberOfSheets();
    for(int i = 0; i < sheetCount; i++) {
      String sheetName = excelWorkbook.getSheetAt(i).getSheetName();
      if(!sheetNames.contains(sheetName) && !SHEET_RESERVED_NAMES.contains(sheetName)) {
        valueTablesMapOnInit.put(sheetName, new ExcelValueTable(this, sheetName, "Participant"));
      }
    }

    return valueTablesMapOnInit.keySet();
  }

  private List<ExcelDatasourceParsingException> readValueTablesFromVariableSheet(
      Map<String, Integer> headerMapVariables, Collection<String> sheetNames) {
    List<ExcelDatasourceParsingException> errors = new ArrayList<ExcelDatasourceParsingException>();

    for(int i = 1; i < getVariablesSheet().getPhysicalNumberOfRows(); i++) {
      Row variableRow = getVariablesSheet().getRow(i);
      String tableHeader = ExcelUtil.findNormalizedHeader(headerMapVariables.keySet(), VariableConverter.TABLE);
      String tableName = DEFAULT_TABLE_NAME;
      if(tableHeader != null) {
        tableName = ExcelUtil.getCellValueAsString(variableRow.getCell(headerMapVariables.get(tableHeader)));
        if(tableName.trim().isEmpty()) {
          errors.add(new ExcelDatasourceParsingException("Table name is required", //
              "TableNameRequired", getVariablesSheet().getSheetName(), i + 1));
        }
      }
      if(!valueTablesMapOnInit.containsKey(tableName)) {
        String entityTypeHeader = ExcelUtil
            .findNormalizedHeader(headerMapVariables.keySet(), VariableConverter.ENTITY_TYPE);
        String entityType = "Participant";
        if(entityTypeHeader != null) {
          entityType = ExcelUtil.getCellValueAsString(variableRow.getCell(headerMapVariables.get(entityTypeHeader)));
        }
        valueTablesMapOnInit.put(tableName, new ExcelValueTable(this, tableName, entityType));
        sheetNames.add(getSheetName(tableName));
      }
    }

    return errors;
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return valueTablesMapOnInit.get(tableName);
  }

  public boolean hasVariablesSheet() {
    return excelWorkbook.getSheet("Variables") != null;
  }

  public Sheet getVariablesSheet() {
    if(variablesSheet == null) {
      variablesSheet = createSheetIfNotExist("Variables");
    }
    return variablesSheet;
  }

  public Sheet getCategoriesSheet() {
    if(categoriesSheet == null) {
      categoriesSheet = createSheetIfNotExist("Categories");
    }
    return categoriesSheet;
  }

  public Set<String> getVariablesCustomAttributeNames() {
    return getCustomAttributeNames(getVariablesSheet().getRow(0), VariableConverter.reservedVariableHeaders);
  }

  public Set<String> getCategoriesCustomAttributeNames() {
    return getCustomAttributeNames(getCategoriesSheet().getRow(0), VariableConverter.reservedCategoryHeaders);
  }

  private Set<String> getCustomAttributeNames(Row rowHeader, Iterable<String> reservedAttributeNames) {
    Set<String> attributesNames = new HashSet<String>();
    int cellCount = rowHeader.getPhysicalNumberOfCells();
    for(int i = 0; i < cellCount; i++) {
      String attributeName = ExcelUtil.getCellValueAsString(rowHeader.getCell(i)).trim();
      if(ExcelUtil.findNormalizedHeader(reservedAttributeNames, attributeName) == null) {
        attributesNames.add(attributeName);
      }
    }
    return attributesNames;
  }

  public Map<String, Integer> getVariablesHeaderMap() {
    return getMapSheetHeader(getVariablesSheet().getRow(0));
  }

  public Map<String, Integer> getCategoriesHeaderMap() {
    return getMapSheetHeader(getCategoriesSheet().getRow(0));
  }

  private Map<String, Integer> getMapSheetHeader(Row rowHeader) {
    Map<String, Integer> headerMap = null;
    if(rowHeader != null) {
      headerMap = new HashMap<String, Integer>();
      int cellCount = rowHeader.getPhysicalNumberOfCells();
      Cell cell;

      for(int i = 0; i < cellCount; i++) {
        cell = rowHeader.getCell(i);
        headerMap.put(cell.getStringCellValue().trim(), i);
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
   *
   * @param tableName
   * @return
   */
  private String getSheetName(String tableName) {
    String sheetName = NameConverter.toExcelName(tableName);
    // Excel allows a maximum of 30 chars for table names
    if(sheetName.length() > SHEET_NAME_MAX_LENGTH) {
      sheetName = sheetName.substring(0, 27) + "$" + (sheetName.length() - SHEET_NAME_MAX_LENGTH);
    }
    log.debug("{}={}", tableName, sheetName);
    return sheetName;
  }

  /**
   * Get the sheet from table name.
   *
   * @param tableName
   * @return null if sheet does not exists
   */
  Sheet getSheet(String tableName) {
    return excelWorkbook.getSheet(getSheetName(tableName));
  }

  private void createExcelStyles() {
    excelStyles = new HashMap<String, CellStyle>();

    CellStyle headerCellStyle = excelWorkbook.createCellStyle();
    Font headerFont = excelWorkbook.createFont();
    headerFont.setBoldweight((short) BOLD_WEIGHT);
    headerCellStyle.setFont(headerFont);

    excelStyles.put("headerCellStyle", headerCellStyle);
  }

  @Override
  @Nonnull
  public Timestamps getTimestamps() {
    return new ExcelTimestamps(excelFile);
  }

  public CellStyle getHeaderCellStyle() {
    return excelStyles.get("headerCellStyle");
  }
}

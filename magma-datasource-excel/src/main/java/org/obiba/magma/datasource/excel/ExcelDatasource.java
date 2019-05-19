/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.validation.constraints.NotNull;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.obiba.magma.Disposable;
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
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.collect.Lists;
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

  private final Map<String, ExcelValueTable> valueTablesMapOnInit = new LinkedHashMap<>(100);

  private ExcelValueTableWriter excelValueTableWriter;

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

  private void createWorbookFromFile(){
    if(excelFile.exists()) {
      try (InputStream input = new FileInputStream(excelFile)) {
        createWorkbookFromInputStream(input);
      } catch(IOException e) {
        throw new MagmaRuntimeException("Exception reading excel spreadsheet " + excelFile.getName(), e);
      }
    } else {
      if(excelFile.getName().endsWith("xls"))
        log.warn(
            "Creating an ExcelDatasource using Excel 97 format which only supports 256 columns. This may not be sufficient for large amounts of variables. Specify a filename with an extension other than 'xls' to use Excel 2007 format.");

      excelWorkbook = excelFile.getName().endsWith("xls") ? new HSSFWorkbook() : new XSSFWorkbook();
    }
  }

  private void createWorkbookFromInputStream() {
    createWorkbookFromInputStream(excelInput);
  }

  private void createWorkbookFromInputStream(InputStream inpOrig) {
    try(InputStream inp = !inpOrig.markSupported() ? new PushbackInputStream(inpOrig, 8) : inpOrig) {
      if(POIFSFileSystem.hasPOIFSHeader(inp)) {
        createHSSFWorkbook(inp);
      } else if(POIXMLDocument.hasOOXMLHeader(inp)) {
        createXSSFWorkbook(inp);
      } else {
        excelWorkbook = WorkbookFactory.create(inpOrig);
      }
    } catch(IOException e) {
      throw new MagmaRuntimeException("Exception reading excel spreadsheet " + excelFile.getName(), e);
    } catch(OpenXML4JException | SAXException e) {
      throw new MagmaRuntimeException("Invalid excel spreadsheet format " + excelFile.getName(), e);
    } catch(IllegalArgumentException e) {
      throw new MagmaRuntimeException(
          "Invalid excel spreadsheet format from input stream (neither an OLE2 stream nor an OOXML stream).");
    }
  }

  private void createHSSFWorkbook(InputStream inp) throws IOException {
    POIFSFileSystem poifs = new POIFSFileSystem(inp);

    try (InputStream din = poifs.createDocumentInputStream("Workbook")) {
      HSSFRequest req = new HSSFRequest();
      excelWorkbook = new HSSFWorkbook();
      req.addListenerForAllRecords(new SheetExtractorListener(excelWorkbook, VARIABLES_SHEET, CATEGORIES_SHEET));
      HSSFEventFactory factory = new HSSFEventFactory();
      factory.processEvents(req, din);
    }
  }

  private void createXSSFWorkbook(InputStream inp) throws IOException, SAXException, OpenXML4JException {
    excelWorkbook = new XSSFWorkbook();
    OPCPackage container = OPCPackage.open(inp);
    ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(container);
    XSSFReader reader = new XSSFReader(container);
    parseSheets(reader, strings, excelWorkbook, VARIABLES_SHEET, CATEGORIES_SHEET);
  }

  private void parseSheets(XSSFReader reader, ReadOnlySharedStringsTable strings, Workbook excelWorkbook, String... sheetNames) throws SAXException, IOException,
      InvalidFormatException {
    XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();

    while (iter.hasNext()) {
      try (InputStream sheet = iter.next()) {
        String sName = iter.getSheetName();

        if(Arrays.asList(sheetNames).contains(sName)) {
          XMLReader parser = buildSheetParser(strings, reader.getStylesTable(), excelWorkbook, sName);
          parser.parse(new InputSource(sheet));
        }
      }
    }
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
  @NotNull
  public ValueTableWriter createWriter(@NotNull String name, @NotNull String entityType) {

    ExcelValueTable valueTable;

    if(hasValueTable(name)) {
      valueTable = (ExcelValueTable) getValueTable(name);
    } else {
      addValueTable(valueTable = new ExcelValueTable(this, name, entityType));
    }

    if (excelValueTableWriter == null)
      excelValueTableWriter = new ExcelValueTableWriter(valueTable);
    else
      excelValueTableWriter = new ExcelValueTableWriter(valueTable, excelValueTableWriter);

    return excelValueTableWriter;
  }

  @Override
  public void dispose() {
    if (excelValueTableWriter != null && excelValueTableWriter.writeVariables() != null)
      ((Disposable) excelValueTableWriter.writeVariables()).dispose();
    super.dispose();
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
    try(OutputStream out = excelFile == null ? excelOutput : new FileOutputStream(excelFile)) {
      if(out != null) {
        writeWorkbook(out);
      }
    } catch(Exception e) {
      throw new MagmaRuntimeException("Could not write to excel output stream", e);
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    Collection<String> sheetNames = new LinkedHashSet<>(100);

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
    for(int i = 1; i < getVariablesSheet().getPhysicalNumberOfRows(); i++) {
      Row variableRow = getVariablesSheet().getRow(i);
      readValueTablesFromVariableRow(headerMapVariables, sheetNames, variableRow);
    }
    return Lists.newArrayList();
  }

  private void readValueTablesFromVariableRow(Map<String, Integer> headerMapVariables, Collection<String> sheetNames, Row variableRow) {
    if (variableRow == null) return;
    String tableHeader = ExcelUtil.findNormalizedHeader(headerMapVariables.keySet(), VariableConverter.TABLE);
    String tableName = DEFAULT_TABLE_NAME;
    if(tableHeader != null) {
      tableName = ExcelUtil.getCellValueAsString(variableRow.getCell(headerMapVariables.get(tableHeader)));
      if(tableName.trim().isEmpty()) {
        return; // ignore rows without table name
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
    return getCustomAttributeNames(getVariablesSheet(), VariableConverter.reservedVariableHeaders);
  }

  public Set<String> getCategoriesCustomAttributeNames() {
    return getCustomAttributeNames(getCategoriesSheet(), VariableConverter.reservedCategoryHeaders);
  }

  private Set<String> getCustomAttributeNames(Sheet sheet, Iterable<String> reservedAttributeNames) {
    Row rowHeader = sheet.getRow(0);
    Set<String> attributesNames = new HashSet<>();
    int cellCount = rowHeader.getPhysicalNumberOfCells();

    for(int i = 0; i < cellCount; i++) {
      Cell c = rowHeader.getCell(i, Row.RETURN_BLANK_AS_NULL);

      if (c == null) {
        throw new MagmaRuntimeException("Missing header: " + sheet.getSheetName() + " at column " + colToName(i));
      }

      String attributeName = ExcelUtil.getCellValueAsString(c).trim();

      if(ExcelUtil.findNormalizedHeader(reservedAttributeNames, attributeName) == null) {
        attributesNames.add(attributeName);
      }
    }

    return attributesNames;
  }

  public Map<String, Integer> getVariablesHeaderMap() {
    return getMapSheetHeader(getVariablesSheet());
  }

  public Map<String, Integer> getCategoriesHeaderMap() {
    return getMapSheetHeader(getCategoriesSheet());
  }

  private Map<String, Integer> getMapSheetHeader(Sheet sheet) {
    Row rowHeader = sheet.getRow(0);
    Map<String, Integer> headerMap = null;

    if(rowHeader != null) {
      headerMap = new HashMap<>();
      int cellCount = rowHeader.getPhysicalNumberOfCells();
      Cell cell;

      for(int i = 0; i < cellCount; i++) {
        cell = rowHeader.getCell(i, Row.RETURN_BLANK_AS_NULL);

        if (cell == null) {
          throw new MagmaRuntimeException("Missing header: " + sheet.getSheetName() + " at column " + colToName(i));
        }

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
    excelStyles = new HashMap<>();

    CellStyle headerCellStyle = excelWorkbook.createCellStyle();
    Font headerFont = excelWorkbook.createFont();
    headerFont.setBoldweight((short) BOLD_WEIGHT);
    headerCellStyle.setFont(headerFont);

    excelStyles.put("headerCellStyle", headerCellStyle);
  }

  @Override
  @NotNull
  public Timestamps getTimestamps() {
    return new ExcelTimestamps(excelFile);
  }

  public CellStyle getHeaderCellStyle() {
    return excelStyles.get("headerCellStyle");
  }

  private String colToName(int colNum) {
    String colName = "";
    int colNumTemp = colNum;

    do {
      colName = String.valueOf(Character.toChars('A' + (colNumTemp % 26))) + colName;
      colNumTemp = Math.floorDiv(colNumTemp, 26) - 1;
    } while (colNumTemp >= 0);

    return  colName;
  }

  private XMLReader buildSheetParser(ReadOnlySharedStringsTable strings, StylesTable st, Workbook workbook, String name) throws SAXException {
    XMLReader parser = XMLReaderFactory.createXMLReader();
    ContentHandler handler = new SheetHandler(strings, st, workbook, name);
    parser.setContentHandler(handler);

    return parser;
  }

  enum xssfDataType {
    BOOL,
    ERROR,
    FORMULA,
    INLINESTR,
    SSTINDEX,
    NUMBER,
  }

  private static class SheetHandler extends DefaultHandler {
    private Workbook workbook;
    private Sheet sh;
    private Row row;
    private StylesTable stylesTable;
    private ReadOnlySharedStringsTable sharedStringsTable;

    private boolean vIsOpen;
    private xssfDataType nextDataType;
    private short formatIndex;
    private String formatString;
    private final DataFormatter formatter;
    private int thisColumn = -1;
    private int lastColumnNumber = -1;
    private StringBuffer value;

    private SheetHandler(ReadOnlySharedStringsTable sst, StylesTable st, Workbook workbook, String name) {
      this.sharedStringsTable = sst;
      this.stylesTable = st;
      this.workbook = workbook;
      sh = workbook.createSheet(name);
      row = sh.createRow(0);
      this.value = new StringBuffer();
      this.formatter = new DataFormatter();
    }

    @Override
    @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
      if ("inlineStr".equals(name) || "v".equals(name)) {
        vIsOpen = true;
        value.setLength(0);
      }

      else if ("c".equals(name)) {
        String r = attributes.getValue("r");

        int firstDigit = -1;

        for (int c = 0; c < r.length(); ++c) {
          if (Character.isDigit(r.charAt(c))) {
            firstDigit = c;
            break;
          }
        }

        thisColumn = nameToColumn(r.substring(0, firstDigit));
        this.nextDataType = xssfDataType.NUMBER;
        this.formatIndex = -1;
        this.formatString = null;

        String cellType = attributes.getValue("t");
        String cellStyleStr = attributes.getValue("s");

        if ("b".equals(cellType))
          nextDataType = xssfDataType.BOOL;
        else if ("e".equals(cellType))
          nextDataType = xssfDataType.ERROR;
        else if ("inlineStr".equals(cellType))
          nextDataType = xssfDataType.INLINESTR;
        else if ("s".equals(cellType))
          nextDataType = xssfDataType.SSTINDEX;
        else if ("str".equals(cellType))
          nextDataType = xssfDataType.FORMULA;
        else if (cellStyleStr != null) {
          int styleIndex = Integer.parseInt(cellStyleStr);
          XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
          this.formatIndex = style.getDataFormat();
          this.formatString = style.getDataFormatString();
          if (this.formatString == null)
            this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
        }
      }
    }

    @Override
    @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
    public void endElement(String uri, String localName, String name) throws SAXException {
      String thisStr;

      if ("v".equals(name)) {
        switch (nextDataType) {
          case BOOL:
            char first = value.charAt(0);
            thisStr = "" + first;
            break;

          case ERROR:
            thisStr = value.toString();
            break;

          case FORMULA:
            thisStr = value.toString();
            break;

          case INLINESTR:
            XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
            thisStr = rtsi.toString();
            break;

          case SSTINDEX:
            String sstIndex = value.toString();
            try {
              int idx = Integer.parseInt(sstIndex);
              XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));
              thisStr = rtss.toString();
            }
            catch (NumberFormatException ex) {
              throw new MagmaRuntimeException("Failed to parse SST index '" + sstIndex + "': " + ex.toString());
            }

            break;

          case NUMBER:
            String n = value.toString();
            if (this.formatString != null)
              thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex, this.formatString);
            else
              thisStr = n;
            break;

          default:
            thisStr = nextDataType.toString();
            break;
        }

        Cell cell = row.createCell(thisColumn);
        cell.setCellValue(thisStr);

        if (thisColumn > -1)
          lastColumnNumber = thisColumn;

      } else if ("row".equals(name)) {
        lastColumnNumber = -1;
        row = sh.createRow(sh.getPhysicalNumberOfRows());
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (vIsOpen)
        value.append(ch, start, length);
    }

    private int nameToColumn(String name) {
      int column = -1;

      for (int i = 0; i < name.length(); ++i) {
        int c = name.charAt(i);
        column = (column + 1) * 26 + c - 'A';
      }

      return column;
    }
  }

  private static class SheetExtractorListener implements HSSFListener {
    private SSTRecord sstrec;
    private Workbook workbook;
    private LinkedList<Sheet> sheets = new LinkedList<>();
    private Sheet sheet;
    private List<String> sheetNames;

    public SheetExtractorListener(Workbook workbook, String... sheetNames) {
      this.workbook = workbook;
      this.sheetNames = Lists.newArrayList(sheetNames);
    }

    /**
     *
     * @param record
     */
    @Override
    @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
    public void processRecord(Record record) {
      switch(record.getSid()) {
        case BOFRecord.sid:
          BOFRecord bof = (BOFRecord) record;

          if (bof.getType() == bof.TYPE_WORKBOOK)
          {
            //ignore
          } else if (bof.getType() == bof.TYPE_WORKSHEET) {
            sheet = sheets.poll();
          }

          break;
        case BoundSheetRecord.sid:
          BoundSheetRecord bsr = (BoundSheetRecord) record;

          if (sheetNames.contains(bsr.getSheetname())) {
            sheets.add(this.workbook.createSheet(bsr.getSheetname()));
          } else {
            sheets.add(null);
          }

          break;
        case RowRecord.sid:
          RowRecord rowrec = (RowRecord) record;

          if(sheet != null) {
            sheet.createRow(rowrec.getRowNumber());
          }

          break;
        case NumberRecord.sid:
          NumberRecord numrec = (NumberRecord) record;

          if(sheet != null) {
            sheet.getRow(numrec.getRow()).createCell(numrec.getColumn()).setCellValue(numrec.getValue());
          }

          break;
        case SSTRecord.sid:
          sstrec = (SSTRecord) record;
          break;
        case LabelSSTRecord.sid:
          LabelSSTRecord lrec = (LabelSSTRecord) record;

          if(sheet != null) {
            sheet.getRow(lrec.getRow()).createCell(lrec.getColumn())
                .setCellValue(sstrec.getString(lrec.getSSTIndex()).getString());
          }

          break;
      }
    }
  }
}

package org.obiba.magma.datasource.excel.support;

public class ExcelDatasourceParsingException extends Exception {

  private static final long serialVersionUID = 1L;

  private String sheet;

  private int row;

  /**
   * @param sheet
   * @param row
   * @param message
   */
  public ExcelDatasourceParsingException(String sheet, int row, String message) {
    super(message);
    this.sheet = sheet;
    this.row = row;
  }

  public ExcelDatasourceParsingException(String sheet, int row, String message, Throwable e) {
    super(message, e);
    this.sheet = sheet;
    this.row = row;
  }

  public int getRow() {
    return row;
  }

  public String getSheet() {
    return sheet;
  }
}

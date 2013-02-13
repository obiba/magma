package org.obiba.magma.datasource.csv.support;

import org.obiba.magma.support.DatasourceParsingException;

public class CsvDatasourceParsingException extends DatasourceParsingException {

  private static final long serialVersionUID = 1L;

  /**
   * 
   * @param message default message
   * @param messageKey message key for localization
   * @param row row number in the sheet
   * @param parameters parameters to go in the localized message place holders
   */
  public CsvDatasourceParsingException(String message, String messageKey, int row, Object... parameters) {
    super(message + " (" + row + ")", messageKey, parameters);
    getParameters().add(0, row);
  }

  /**
   * 
   * @param message default message
   * @param e cause exception
   * @param messageKey message key for localization
   * @param row row number in the sheet
   * @param parameters parameters to go in the localized message place holders
   */
  public CsvDatasourceParsingException(String message, Throwable e, String messageKey, int row, Object... parameters) {
    super(message + " (" + row + ")", e, messageKey, parameters);
    getParameters().add(0, row);
  }

  /**
   *
   * @param message default message
   * @param e cause exception
   * @param messageKey message key for localization
   * @param parameters parameters to go in the localized message place holders
   */
  public CsvDatasourceParsingException(String message, Throwable e, String messageKey, Object... parameters) {
    super(message, e, messageKey, parameters);
  }

}

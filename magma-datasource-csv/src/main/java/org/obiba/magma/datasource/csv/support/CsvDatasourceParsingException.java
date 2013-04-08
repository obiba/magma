package org.obiba.magma.datasource.csv.support;

import javax.annotation.Nullable;

import org.obiba.magma.support.DatasourceParsingException;

public class CsvDatasourceParsingException extends DatasourceParsingException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message default message
   * @param messageKey message key for localization
   * @param row row number in the sheet
   * @param parameters parameters to go in the localized message place holders
   */
  public CsvDatasourceParsingException(String message, String messageKey, int row, @Nullable Object... parameters) {
    super(createMessage(message, row), messageKey, parameters);
    if(row > 0) getParameters().add(0, row);
  }

  /**
   * @param message default message
   * @param e cause exception
   * @param messageKey message key for localization
   * @param row row number in the sheet
   * @param parameters parameters to go in the localized message place holders
   */
  public CsvDatasourceParsingException(String message, Throwable e, String messageKey, int row,
      @Nullable Object... parameters) {
    super(createMessage(message, row), e, messageKey, parameters);
    if(row > 0) getParameters().add(0, row);
  }

  /**
   * Helper function to build an error message with the row number if supplied
   *
   * @param errorMessage
   * @param row -1 implies to exclude the row number from message
   */
  private static String createMessage(String errorMessage, int row) {
    return row > -1 ? errorMessage + " (" + row + ")" : errorMessage;
  }

}

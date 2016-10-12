/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.excel.support;

import org.obiba.magma.support.DatasourceParsingException;

public class ExcelDatasourceParsingException extends DatasourceParsingException {

  private static final long serialVersionUID = 1L;

  /**
   * @param message default message
   * @param messageKey message key for localization
   * @param sheet sheet name
   * @param row row number in the sheet
   * @param parameters parameters to go in the localized message place holders
   */
  public ExcelDatasourceParsingException(String message, String messageKey, String sheet, int row,
      Object... parameters) {
    super(message + " (" + sheet + ":" + row + ")", messageKey, parameters);
    getParameters().add(0, sheet);
    getParameters().add(1, row);
  }

  /**
   * @param message default message
   * @param e cause exception
   * @param messageKey message key for localization
   * @param sheet sheet name
   * @param row row number in the sheet
   * @param parameters parameters to go in the localized message place holders
   */
  public ExcelDatasourceParsingException(String message, Throwable e, String messageKey, String sheet, int row,
      Object... parameters) {
    super(message + " (" + sheet + ":" + row + ")", e, messageKey, parameters);
    getParameters().add(0, sheet);
    getParameters().add(1, row);
  }

}

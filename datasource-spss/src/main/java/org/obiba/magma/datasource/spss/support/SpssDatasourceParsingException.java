/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.spss.support;

import org.obiba.magma.support.DatasourceParsingException;

public class SpssDatasourceParsingException extends DatasourceParsingException {

  private static final long serialVersionUID = 1L;


  public SpssDatasourceParsingException(String message, String source, int row, String messageKey, Object... messageArgs) {
    super(message + " (" + source + ":" + row + ")", messageKey, messageArgs);
  }

  public SpssDatasourceParsingException(Throwable e, String messageKey, Object... messageArgs) {
    super(e.getMessage(), e, messageKey, messageArgs);
  }
}

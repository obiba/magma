/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.limesurvey;

import org.obiba.magma.support.DatasourceParsingException;

public class LimesurveyParsingException extends DatasourceParsingException {

  private static final long serialVersionUID = 311488468579483118L;

  public LimesurveyParsingException(String message, String messageKey, Object... parameters) {
    super(message, messageKey, parameters);
  }
}

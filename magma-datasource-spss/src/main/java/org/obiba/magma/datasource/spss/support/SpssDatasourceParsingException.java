/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.spss.support;

import javax.annotation.Nullable;

import org.obiba.magma.support.DatasourceParsingException;

import com.google.common.base.Strings;

public class SpssDatasourceParsingException extends DatasourceParsingException {

  private static final long serialVersionUID = -3197572243597757771L;
  private String variableInfo;
  private String extraInfo;

  public SpssDatasourceParsingException(String message, String messageKey, @Nullable Object... parameters) {
    super(message, messageKey, parameters);
  }

  @Override
  public String getMessage() {
    return super.getMessage() + (Strings.isNullOrEmpty(variableInfo) ? "" : variableInfo) +
        (Strings.isNullOrEmpty(extraInfo) ? "" : extraInfo);
  }

  public SpssDatasourceParsingException extraInfo(SpssInvalidCharacterException e) {
    extraInfo = String.format(" (String with invalid characters: '%s')", e.getSource());
    return this;
  }

  public SpssDatasourceParsingException extraInfo(String extra) {
    extraInfo = String.format(" ('%s')", extra);
    return this;
  }

  public SpssDatasourceParsingException metadataInfo(String variableName, int variableIndex) {
    variableInfo = String.format(" (Variable info: name='%s' @ row='%d')", variableName, variableIndex);
    return this;
  }

  public SpssDatasourceParsingException dataInfo(String variableName, int variableIndex) {
    variableInfo = String.format(" (Data info: variable='%s' @ row='%d')", variableName, variableIndex);
    return this;
  }

}

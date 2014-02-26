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

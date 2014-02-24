package org.obiba.magma.datasource.spss.support;

import javax.annotation.Nullable;

import org.obiba.magma.support.DatasourceParsingException;

public class SpssDatasourceParsingException extends DatasourceParsingException {

  private static final long serialVersionUID = -3197572243597757771L;

  public SpssDatasourceParsingException(String message, String messageKey, @Nullable Object... parameters) {
    super(message, messageKey, parameters);
  }

  public SpssDatasourceParsingException(String message, SpssInvalidCharacterException e, int variableIndex,
      String variableName, String messageKey, Object... parameters) {
    this(message + buildInvalidCharacterInfo(e) + buildVariableInfo(variableIndex, variableName), messageKey,
        parameters);
  }

  public SpssDatasourceParsingException(String message, int variableIndex, String variableName, String messageKey,
      Object... parameters) {
    this(message + buildVariableInfo(variableIndex, variableName), messageKey, parameters);
  }

  private static String buildVariableInfo(int variableIndex, String variableName) {
    return " (row: '" + variableIndex + "' variable: '" + variableName +"')";
  }

  private static String buildInvalidCharacterInfo(SpssInvalidCharacterException e) {
    return " (invalid string '" + e.getSource() + "')";
  }

}

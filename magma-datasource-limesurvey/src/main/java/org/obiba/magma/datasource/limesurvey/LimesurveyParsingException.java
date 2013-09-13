package org.obiba.magma.datasource.limesurvey;

import org.obiba.magma.support.DatasourceParsingException;

public class LimesurveyParsingException extends DatasourceParsingException {

  private static final long serialVersionUID = 311488468579483118L;

  public LimesurveyParsingException(String message, String messageKey, Object... parameters) {
    super(message, messageKey, parameters);
  }
}

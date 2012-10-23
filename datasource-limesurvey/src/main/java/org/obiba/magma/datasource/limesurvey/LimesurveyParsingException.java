package org.obiba.magma.datasource.limesurvey;

import org.obiba.magma.support.DatasourceParsingException;

public class LimesurveyParsingException extends DatasourceParsingException {
  public LimesurveyParsingException(String message, String messageKey, Object... parameters) {
    super(message, messageKey, parameters);
  }
}

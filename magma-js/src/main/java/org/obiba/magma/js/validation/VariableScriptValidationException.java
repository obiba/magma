package org.obiba.magma.js.validation;

import org.obiba.magma.js.MagmaJsEvaluationRuntimeException;

public class VariableScriptValidationException extends MagmaJsEvaluationRuntimeException {

  private static final long serialVersionUID = 8198969173772269294L;

  public VariableScriptValidationException(String message) {
    super(message);
  }

  public VariableScriptValidationException(Throwable cause) {
    super(cause);
  }
}

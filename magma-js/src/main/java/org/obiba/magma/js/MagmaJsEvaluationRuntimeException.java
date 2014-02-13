package org.obiba.magma.js;

import org.obiba.magma.MagmaRuntimeException;

public class MagmaJsEvaluationRuntimeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 1429416314609430881L;

  public MagmaJsEvaluationRuntimeException(String message) {
    super(message);
  }

  public MagmaJsEvaluationRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public MagmaJsEvaluationRuntimeException(Throwable cause) {
    super(cause);
  }
}

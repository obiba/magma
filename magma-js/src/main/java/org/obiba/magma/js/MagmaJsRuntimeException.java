package org.obiba.magma.js;

import org.obiba.magma.MagmaRuntimeException;

public class MagmaJsRuntimeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 1429416314609430881L;

  public MagmaJsRuntimeException(String message) {
    super(message);
  }

  public MagmaJsRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}

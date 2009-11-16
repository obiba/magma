package org.obiba.magma;

public class MagmaRuntimeException extends RuntimeException {

  public MagmaRuntimeException() {
    super();
  }

  public MagmaRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public MagmaRuntimeException(String message) {
    super(message);
  }

  public MagmaRuntimeException(Throwable cause) {
    super(cause);
  }

}

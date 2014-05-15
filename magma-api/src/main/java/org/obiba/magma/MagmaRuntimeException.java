package org.obiba.magma;

/**
 * Base class for all Magma runtime exceptions. Any exception thrown by Magma runtime should extend this class.
 */
public class MagmaRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -1825626210129821160L;

  public MagmaRuntimeException() {
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

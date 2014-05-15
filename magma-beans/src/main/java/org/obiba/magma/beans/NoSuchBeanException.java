package org.obiba.magma.beans;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueSet;

public class NoSuchBeanException extends MagmaRuntimeException {

  private static final long serialVersionUID = 559153486087896008L;

  public NoSuchBeanException(ValueSet valueSet, Class<?> beanType, String message) {
    super(message);
  }
}

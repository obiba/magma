package org.obiba.meta.beans;

import org.obiba.meta.ValueSet;

public class NoSuchBeanException extends RuntimeException {

  private static final long serialVersionUID = 559153486087896008L;

  public NoSuchBeanException(ValueSet valueSet, Class<?> beanType, String message) {
    super(message);
  }
}

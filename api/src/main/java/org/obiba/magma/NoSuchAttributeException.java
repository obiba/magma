package org.obiba.magma;

import java.util.Locale;

public class NoSuchAttributeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  public NoSuchAttributeException(String attribute, String attributeAware) {
    super("No such attribute '" + attribute + "' for '" + attributeAware + "'");
  }

  public NoSuchAttributeException(String attribute, Locale locale, String attributeAware) {
    super("No such attribute '" + attribute + "'@" + locale.toString() + " for '" + attributeAware + "'");
  }

}

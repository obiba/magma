package org.obiba.magma;

import java.util.Locale;

public class NoSuchAttributeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private String attributeName;

  public NoSuchAttributeException(String attribute, String attributeAware) {
    super("No such attribute '" + attribute + "' for '" + attributeAware + "'");
    this.attributeName = attribute;
  }

  public NoSuchAttributeException(String attribute, Locale locale, String attributeAware) {
    super("No such attribute '" + attribute + "'@" + locale.toString() + " for '" + attributeAware + "'");
    this.attributeName = attribute;
  }

  public String getAttributeName() {
    return attributeName;
  }

}

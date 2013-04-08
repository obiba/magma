package org.obiba.magma;

import java.util.Locale;

public class NoSuchAttributeException extends MagmaRuntimeException {

  private static final long serialVersionUID = 5887330656285998606L;

  private final String attributeName;

  public NoSuchAttributeException(String attribute, String attributeAware) {
    this(attribute, null, attributeAware);
  }

  public NoSuchAttributeException(String attribute, Locale locale, String attributeAware) {
    super("No such attribute '" + attribute + (locale != null ? "'@" + locale.toString() : "'") + " for '" +
        attributeAware + "'");
    attributeName = attribute;
  }

  public String getAttributeName() {
    return attributeName;
  }

}

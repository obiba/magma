package org.obiba.magma;

import java.util.List;
import java.util.Locale;

public interface AttributeAware {

  /**
   * Returns true if this instance has at least one {@code Attribute} (with any name).
   * @return true when this instance has at least one {@code Attribute}
   */
  public boolean hasAttributes();

  /**
   * Returns true if this instance has at least one {@code Attribute} with the specified name.
   * @return true when this instance has at least one {@code Attribute} with the specified name; false otherwise.
   */
  public boolean hasAttribute(String name);

  /**
   * Returns the first attribute associated with the specified name. Note that multiple instances of {@code Attribute}
   * can have the same name. This method will always return the first one.
   * @param name
   * @return
   */
  public Attribute getAttribute(String name) throws NoSuchAttributeException;

  public Attribute getAttribute(String name, Locale locale) throws NoSuchAttributeException;

  public String getAttributeStringValue(String name) throws NoSuchAttributeException;

  public List<Attribute> getAttributes();

}

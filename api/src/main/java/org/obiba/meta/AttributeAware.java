package org.obiba.meta;

import java.util.List;
import java.util.Locale;

public interface AttributeAware {

  /**
   * Returns true if this instance has at least one {@code Attribute} (with any name).
   * @return
   */
  public boolean hasAttributes();

  public boolean hasAttribute(String name);

  public Attribute getAttribute(String name);

  public Attribute getAttribute(String name, Locale locale);

  public List<Attribute> getAttributes();

}

package org.obiba.magma;

import java.util.Locale;

public abstract class AttributeAwareBuilder<T extends AttributeAwareBuilder<?>> {

  public T addAttribute(String name, String value) {
    getAttributeAware().attributes.put(name, Attribute.Builder.newAttribute(name).withValue(value).build());
    return getBuilder();
  }

  public T addAttribute(String name, String value, Locale locale) {
    getAttributeAware().attributes.put(name, Attribute.Builder.newAttribute(name).withValue(locale, value).build());
    return getBuilder();
  }

  public T addAttribute(Attribute attribute) {
    getAttributeAware().attributes.put(attribute.getName(), attribute);
    return getBuilder();
  }

  protected abstract AbstractAttributeAware getAttributeAware();

  protected abstract T getBuilder();
}

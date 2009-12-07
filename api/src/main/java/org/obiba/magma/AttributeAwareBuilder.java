package org.obiba.magma;

import java.util.Locale;

import com.google.common.collect.ListMultimap;

public abstract class AttributeAwareBuilder<T extends AttributeAwareBuilder<?>> {

  public T addAttribute(String name, String value) {
    getAttributes().put(name, Attribute.Builder.newAttribute(name).withValue(value).build());
    return getBuilder();
  }

  public T addAttribute(String name, String value, Locale locale) {
    getAttributes().put(name, Attribute.Builder.newAttribute(name).withValue(locale, value).build());
    return getBuilder();
  }

  public T addAttribute(Attribute attribute) {
    getAttributes().put(attribute.getName(), attribute);
    return getBuilder();
  }

  protected abstract ListMultimap<String, Attribute> getAttributes();

  protected abstract T getBuilder();
}

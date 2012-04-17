package org.obiba.magma;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public abstract class AttributeAwareBuilder<T extends AttributeAwareBuilder<?>> {

  public T addAttribute(String name, String value) {
    getAttributes().put(name, Attribute.Builder.newAttribute(name).withValue(value).build());
    return getBuilder();
  }

  public T addAttribute(String name, String value, Locale locale) {
    if(locale != null && "".equals(locale.toString())) {
      addAttribute(name, value);
    } else {
      getAttributes().put(name, Attribute.Builder.newAttribute(name).withValue(locale, value).build());
    }
    return getBuilder();
  }

  public T addAttribute(Attribute attribute) {
    getAttributes().put(attribute.getName(), attribute);
    return getBuilder();
  }

  public T addAttributes(Iterable<Attribute> attributes) {
    for(Attribute attribute : attributes) {
      addAttribute(attribute);
    }
    return getBuilder();
  }

  protected abstract ListMultimap<String, Attribute> getAttributes();

  protected abstract T getBuilder();

  public static ListMultimap<String, Attribute> overrideAttributes(List<Attribute> existingAttributes,
      List<Attribute> overrideAttributes) {
    ListMultimap<String, Attribute> existingAttributesMultimap = LinkedListMultimap.create();
    for(Attribute attribute : existingAttributes) {
      existingAttributesMultimap.put(attribute.getName(), attribute);
    }
    return overrideAttributes(existingAttributesMultimap, overrideAttributes);
  }

  public static ListMultimap<String, Attribute> overrideAttributes(ListMultimap<String, Attribute> existingAttributes,
      List<Attribute> overrideAttributes) {
    for(Attribute attribute : overrideAttributes) {
      overrideAttribute(existingAttributes, attribute);
    }
    return existingAttributes;
  }

  private static void overrideAttribute(ListMultimap<String, Attribute> attrs, Attribute attribute) {
    if(!attrs.containsEntry(attribute.getName(), attribute)) {
      if(attrs.containsKey(attribute.getName())) {
        if(attribute.isLocalised()) {
          removeLocalisedAttribute(attrs, attribute);
        } else {
          attrs.get(attribute.getName()).remove(0);
        }
      }
      attrs.put(attribute.getName(), attribute);
    }
  }

  private static void removeLocalisedAttribute(ListMultimap<String, Attribute> attrs, final Attribute attribute) {
    try {
      Attribute attributeToRemove = Iterables.find(attrs.get(attribute.getName()), new Predicate<Attribute>() {
        @Override
        public boolean apply(Attribute input) {
          return attribute.getName().equals(input.getName()) && attribute.getLocale().equals(input.getLocale());
        }
      });
      attrs.remove(attributeToRemove.getName(), attributeToRemove);
    } catch(NoSuchElementException e) {
      // Nothing to remove.
    }
  }
}

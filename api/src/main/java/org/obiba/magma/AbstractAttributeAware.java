package org.obiba.meta;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public abstract class AbstractAttributeAware implements AttributeAware {

  ListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

  abstract String getName();

  public boolean hasAttribute(final String name) {
    return attributes.containsKey(name);
  }

  @Override
  public Attribute getAttribute(final String name) {
    try {
      return Iterables.get(attributes.get(name), 0);
    } catch(IndexOutOfBoundsException e) {
      throw new NoSuchAttributeException(name, getName());
    }
  }

  @Override
  public Attribute getAttribute(final String name, final Locale locale) {
    try {
      return Iterables.find(attributes.get(name), new Predicate<Attribute>() {
        @Override
        public boolean apply(Attribute input) {
          return input.isLocalised() && input.getLocale().equals(locale);
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchAttributeException(name, locale, getName());
    }
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return getAttribute(name).getValue().toString();
  }

  @Override
  public List<Attribute> getAttributes() {
    return ImmutableList.copyOf(attributes.values());
  }

  @Override
  public boolean hasAttributes() {
    return attributes.size() > 0;
  }
}

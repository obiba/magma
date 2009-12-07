package org.obiba.magma;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;

public abstract class AbstractAttributeAware implements AttributeAware {

  public abstract String getName();

  public boolean hasAttribute(final String name) {
    return getInstanceAttributes().containsKey(name);
  }

  @Override
  public Attribute getAttribute(final String name) {
    try {
      return Iterables.get(getInstanceAttributes().get(name), 0);
    } catch(IndexOutOfBoundsException e) {
      throw new NoSuchAttributeException(name, getName());
    }
  }

  @Override
  public Attribute getAttribute(final String name, final Locale locale) {
    try {
      return Iterables.find(getInstanceAttributes().get(name), new Predicate<Attribute>() {
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
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    if(hasAttribute(name) == false) throw new NoSuchAttributeException(name, getName());
    return ImmutableList.copyOf(getInstanceAttributes().get(name));
  }

  @Override
  public List<Attribute> getAttributes() {
    return ImmutableList.copyOf(getInstanceAttributes().values());
  }

  @Override
  public boolean hasAttributes() {
    return getInstanceAttributes().size() > 0;
  }

  protected abstract ListMultimap<String, Attribute> getInstanceAttributes();
}

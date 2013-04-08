package org.obiba.magma;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

public abstract class AbstractAttributeAware implements AttributeAware {

  public abstract String getName();

  @Override
  public boolean hasAttribute(String name) {
    return noNamespaceAttributes().containsKey(name);
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public boolean hasAttribute(String name, Locale locale) {
    if(noNamespaceAttributes().containsKey(name)) {
      for(Attribute attribute : noNamespaceAttributes().get(name)) {
        if(attribute.isLocalised() && attribute.getLocale().equals(locale)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean hasAttribute(String namespace, String name) {
    ListMultimap<String, Attribute> nm = namespaceAttributes(namespace);
    return name == null || nm.containsKey(name);
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public boolean hasAttribute(String namespace, String name, Locale locale) {
    for(Attribute attribute : namespaceAttributes(namespace).get(name)) {
      if(attribute.isLocalised() && attribute.getLocale().equals(locale)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Attribute getAttribute(String name) {
    try {
      return Iterables.get(noNamespaceAttributes().get(name), 0);
    } catch(IndexOutOfBoundsException e) {
      throw new NoSuchAttributeException(name, getName());
    }
  }

  @Override
  public Attribute getAttribute(String name, final Locale locale) {
    try {
      return Iterables.find(noNamespaceAttributes().get(name), new Predicate<Attribute>() {
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
  public Attribute getAttribute(String namespace, String name) throws NoSuchAttributeException {
    try {
      return Iterables.get(namespaceAttributes(namespace).get(name), 0);
    } catch(IndexOutOfBoundsException e) {
      throw new NoSuchAttributeException(namespace + "::" + name, getName());
    }
  }

  @Override
  public Attribute getAttribute(String namespace, String name, final Locale locale) throws NoSuchAttributeException {
    try {
      return Iterables.find(namespaceAttributes(namespace).get(name), new Predicate<Attribute>() {
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
  public List<Attribute> getAttributes(String namespace, String name) throws NoSuchAttributeException {
    return ImmutableList.copyOf(namespaceAttributes(namespace).get(name));
  }

  @Override
  public List<Attribute> getNamespaceAttributes(String namespace) throws NoSuchAttributeException {
    return ImmutableList.copyOf(namespaceAttributes(namespace).values());
  }

  @Override
  public Value getAttributeValue(String name) throws NoSuchAttributeException {
    return getAttribute(name).getValue();
  }

  @Override
  public Value getAttributeValue(String namespace, String name) throws NoSuchAttributeException {
    return getAttribute(namespace, name).getValue();
  }

  @Override
  public String getAttributeStringValue(String name) throws NoSuchAttributeException {
    return getAttribute(name).getValue().toString();
  }

  @Override
  public String getAttributeStringValue(String namespace, String name) throws NoSuchAttributeException {
    return getAttribute(namespace, name).getValue().toString();
  }

  @Override
  public List<Attribute> getAttributes(String name) throws NoSuchAttributeException {
    if(!hasAttribute(name)) throw new NoSuchAttributeException(name, getName());
    return ImmutableList.copyOf(noNamespaceAttributes().get(name));
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

  protected ListMultimap<String, Attribute> noNamespaceAttributes() {
    return namespaceAttributes(null);
  }

  /**
   * Returns a view of the attributes for the specified namespace. If namespace is null, this method returns attributes
   * that have no namespace (namespace is null).
   */
  protected ListMultimap<String, Attribute> namespaceAttributes(@Nullable final String namespace) {
    return Multimaps.index(Iterables.filter(getInstanceAttributes().values(), new Predicate<Attribute>() {

      @Override
      public boolean apply(Attribute input) {
        // Allows namespace to be null
        return Objects.equal(namespace, input.hasNamespace() ? input.getNamespace() : null);
      }
    }), AttributeNameFunc.INSTANCE);
  }

  private static final class AttributeNameFunc implements Function<Attribute, String> {

    @SuppressWarnings("TypeMayBeWeakened")
    private static final AttributeNameFunc INSTANCE = new AttributeNameFunc();

    @Override
    public String apply(Attribute input) {
      return input == null ? null : input.getName();
    }

  }
}

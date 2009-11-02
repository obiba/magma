package org.obiba.meta;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

class VariableBean implements Variable {

  String collection;

  String name;

  String entityType;

  String mimeType;

  String unit;

  ValueType valueType;

  String referencedEntityType;

  boolean repeatable;

  String occurrenceGroup;

  ListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

  /** Use a linked hash set to keep insertion order */
  Set<Category> categories = new LinkedHashSet<Category>();

  @Override
  public QName getQName() {
    return new QName(getCollection(), getName());
  }

  @Override
  public String getCollection() {
    return collection;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public boolean isForEntityType(final String type) {
    return getEntityType().equals(type);
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  @Override
  public String getUnit() {
    return unit;
  }

  @Override
  public ValueType getValueType() {
    return valueType;
  }

  public boolean hasAttribute(final String name) {
    return attributes.containsKey(name);
  }

  @Override
  public Attribute getAttribute(final String name) {
    return Iterables.get(attributes.get(name), 0);
  }

  @Override
  public Attribute getAttribute(final String name, final Locale locale) {
    return Iterables.find(attributes.get(name), new Predicate<Attribute>() {
      @Override
      public boolean apply(Attribute input) {
        return input.isLocalised() && input.getLocale().equals(locale);
      }
    });
  }

  @Override
  public List<Attribute> getAttributes() {
    return ImmutableList.copyOf(attributes.values());
  }

  @Override
  public boolean hasAttributes() {
    return attributes.size() > 0;
  }

  @Override
  public String getReferencedEntityType() {
    return referencedEntityType;
  }

  @Override
  public boolean isRepeatable() {
    return repeatable;
  }

  @Override
  public String getOccurrenceGroup() {
    return occurrenceGroup;
  }

  @Override
  public Set<Category> getCategories() {
    return Collections.unmodifiableSet(categories);
  }

  @Override
  public boolean hasCategories() {
    return categories.size() > 0;
  }
}

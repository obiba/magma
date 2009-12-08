package org.obiba.magma;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

class VariableBean extends AbstractAttributeAware implements Variable {

  ListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

  String name;

  String entityType;

  String mimeType;

  String unit;

  ValueType valueType;

  String referencedEntityType;

  boolean repeatable;

  String occurrenceGroup;

  /** Use a linked hash set to keep insertion order */
  Set<Category> categories = new LinkedHashSet<Category>();

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

  @Override
  protected ListMultimap<String, Attribute> getInstanceAttributes() {
    return attributes;
  }
}

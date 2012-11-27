package org.obiba.magma;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

class VariableBean extends AbstractAttributeAware implements Variable, Serializable {

  private static final long serialVersionUID = 1L;

  LinkedListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

  String name;

  String entityType;

  String mimeType;

  String unit;

  ValueType valueType;

  String referencedEntityType;

  boolean repeatable;

  String occurrenceGroup;

  /**
   * Use a linked hash set to keep insertion order
   */
  LinkedHashSet<Category> categories = new LinkedHashSet<Category>();

  transient Map<Value, Category> categoriesAsValue;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public boolean isForEntityType(String type) {
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

  @Override
  public boolean equals(Object o) {
    return o instanceof Variable && ((Variable) o).getName().equals(name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean isMissingValue(Value value) {
    if(value.isNull() || hasCategories() == false) {
      return value.isNull();
    }
    Category c = categoryAsValue().get(value);
    return c != null && c.isMissing();
  }

  private Map<Value, Category> categoryAsValue() {
    if(categoriesAsValue == null) {
      categoriesAsValue = Maps.uniqueIndex(categories, new Function<Category, Value>() {

        @Override
        public Value apply(Category input) {
          return getValueType().valueOf(input.getName());
        }
      });

    }
    return categoriesAsValue;
  }
}

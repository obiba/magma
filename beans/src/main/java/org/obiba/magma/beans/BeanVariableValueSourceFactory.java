/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.magma.beans;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.Variable.BuilderVisitor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.util.Assert;

import com.google.common.base.Functions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;

/**
 * 
 */
public class BeanVariableValueSourceFactory<T> implements VariableValueSourceFactory {

  private Class<T> beanClass;

  private String entityType;

  /** The set of bean properties that are returned as variables */
  private Set<String> properties = Collections.emptySet();

  private String prefix;

  /** Maps property names to variable name */
  private BiMap<String, String> propertyNameToVariableName = HashBiMap.create();

  /** Maps property names to property type */
  private Map<String, Class<?>> propertyNameToPropertyType = new HashMap<String, Class<?>>();

  /** Maps mapped property names to property type */
  private Map<String, Class<?>> mappedPropertyType = new HashMap<String, Class<?>>();

  private Set<? extends BuilderVisitor> variableBuilderVisitors = Collections.emptySet();

  private String occurrenceGroup;

  private Set<VariableValueSource> sources;

  public BeanVariableValueSourceFactory(String entityType, Class<T> beanClass) {
    Assert.notNull(entityType);
    Assert.notNull(beanClass);
    this.entityType = entityType;
    this.beanClass = beanClass;
  }

  public void setProperties(Set<String> properties) {
    Assert.notNull(properties);
    this.properties = properties;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setPropertyNameToVariableName(Map<String, String> propertyNameToVariableName) {
    Assert.notNull(propertyNameToVariableName);
    this.propertyNameToVariableName = HashBiMap.create(propertyNameToVariableName);
  }

  public void setPropertyNameToPropertyType(Map<String, Class<?>> propertyNameToPropertyType) {
    Assert.notNull(propertyNameToPropertyType);
    this.propertyNameToPropertyType = propertyNameToPropertyType;
  }

  public void setMappedPropertyType(Map<String, Class<?>> mappedPropertyType) {
    Assert.notNull(mappedPropertyType);
    this.mappedPropertyType = mappedPropertyType;
  }

  public void setVariableBuilderVisitors(Set<? extends BuilderVisitor> variableBuilderVisitors) {
    Assert.notNull(variableBuilderVisitors);
    this.variableBuilderVisitors = variableBuilderVisitors;
  }

  public void setOccurrenceGroup(String occurrenceGroup) {
    this.occurrenceGroup = occurrenceGroup;
  }

  public Set<VariableValueSource> createSources(String collection) {
    if(sources == null) {
      doBuildVariables(collection);
    }
    return sources;
  }

  /**
   * Finds the type ({@code Class}) for a given {@code propertyName} which may denote a nested property (property path
   * e.g: a.b.c) or mapped property (attribute[key]) or a combination of both (e.g.: a.b[c].d).
   * @param propertyName
   * @return
   */
  protected Class<?> getPropertyType(String propertyName) {
    // Has a property type been explicitly declared? If so, use it.
    Class<?> declaredPropertyType = propertyNameToPropertyType.get(propertyName);
    if(declaredPropertyType != null) {
      return declaredPropertyType;
    }

    Class<?> currentType = getBeanClass();

    String propertyPath = propertyName;
    // Loop as long as the propertyPath designates a nested property
    while(PropertyAccessorUtils.isNestedOrIndexedProperty(propertyPath)) {
      int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);

      String nestedProperty = propertyPath;
      if(pos > -1) {
        nestedProperty = propertyPath.substring(0, pos);
      }

      // Check whether this is a mapped property (a[b])
      if(PropertyAccessorUtils.isNestedOrIndexedProperty(nestedProperty)) {
        // We cannot determine the type of these properties through reflection (even when they contain type parameters
        // i.e. Map<String, String>).
        // The type of these properties has to be specified through configuration
        currentType = getMapAttributeType(PropertyAccessorUtils.getPropertyName(nestedProperty));
        if(pos == -1) {
          return currentType;
        }
        propertyPath = propertyPath.substring(pos + 1);
      } else {
        PropertyDescriptor currentProperty = BeanUtils.getPropertyDescriptor(currentType, nestedProperty);
        if(currentProperty == null) {
          throw new IllegalArgumentException("Invalid path '" + propertyName + "' for type " + getBeanClass().getName() + ": nested property '" + nestedProperty + "' does not exist on type " + currentType.getName());
        }
        // Change the current type so it points to the nested type
        currentType = currentProperty.getPropertyType();
        // Extract the nested type's property path from the original path
        propertyPath = propertyPath.substring(pos + 1);
      }
    }

    // propertyPath is a direct reference to a property of the currentType (no longer a path)
    PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(currentType, propertyPath);
    if(descriptor == null) {
      throw new IllegalArgumentException("Invalid path '" + propertyName + "' for type " + getBeanClass().getName() + ": property '" + propertyPath + "' does not exist on type " + currentType.getName());
    }
    return descriptor.getPropertyType();
  }

  /**
   * Returns the type for values in a mapped property. It is not possible (or at least, I haven't found how) to
   * determine the type of a mapped property (i.e.: property[key]). For this reason, the type to use for such properties
   * has to be specified through configuration. This method must return the type for values in the {@code Map}.
   * 
   * @param propertyName the name of the property (without any key element). Given "property[key]", this method will be
   * passed "property".
   * @return the type of the values in the Map
   */
  protected Class<?> getMapAttributeType(String propertyName) {
    Class<?> type = mappedPropertyType.get(propertyName);
    if(type == null) {
      throw new IllegalStateException("Property '" + propertyName + "' is a mapped property; the value type cannot be determined by relfection. As such, the type of values in the map must be specified through configuration. Use the setMappedPropertyType() method to provide the mapping between property names and value types. Ensure that an entry with the key '" + propertyName + "' exists in this map.");
    }
    return type;
  }

  /**
   * Returns the variable name for a given property name. If none is configured, this method returns the property name
   * as-is.
   * @param propertyName
   * @return
   */
  protected String lookupVariableName(String propertyName) {
    String name = propertyNameToVariableName.get(propertyName);
    return name != null ? prefixName(name) : prefixName(propertyName);
  }

  protected String prefixName(String name) {
    return prefix != null ? prefix + '.' + name : name;
  }

  protected String unprefixName(String name) {
    return prefix != null ? name.replaceFirst(prefix, "") : name;
  }

  /**
   * Returns the property name for a given variable name. If no variable is found, this method returns the variable name
   * as-is.
   * @param name
   * @return
   */
  protected String lookupPropertyName(String name) {
    String propertyName = propertyNameToVariableName.inverse().get(unprefixName(name));
    if(propertyName == null) {
      propertyName = name;
    }
    return propertyName;
  }

  private Class<?> getBeanClass() {
    return beanClass;
  }

  /**
   * Builds the {@code Variable} that this provider supports and also the {@code VariableEntityDataSource} instances for
   * each variable.
   */
  protected synchronized void doBuildVariables(String collection) {
    sources = new HashSet<VariableValueSource>();
    for(String propertyPath : properties) {
      Class<?> propertyType = getPropertyType(propertyPath);
      if(propertyType == null) {
        throw new IllegalArgumentException("Invalid property path'" + propertyPath + "' for type " + getBeanClass().getName());
      }
      sources.add(new BeanPropertyVariableValueSource(doBuildVariable(collection, propertyType, lookupVariableName(propertyPath)), beanClass, propertyPath));
    }
  }

  protected Variable doBuildVariable(String collection, Class<?> propertyType, String name) {
    ValueType type = ValueType.Factory.forClass(propertyType);

    Variable.Builder builder = Variable.Builder.newVariable(collection, name, type, entityType);
    if(propertyType.isEnum()) {
      Enum<?>[] constants = (Enum<?>[]) propertyType.getEnumConstants();
      String[] names = Iterables.toArray(Iterables.transform(Arrays.asList(constants), Functions.toStringFunction()), String.class);
      builder.addCategories(names);
    }

    if(occurrenceGroup != null) {
      builder.repeatable().occurrenceGroup(occurrenceGroup);
    }

    builder.accept(variableBuilderVisitors);

    // Allow extended classes to contribute to the builder
    return buildVariable(builder).build();
  }

  protected Variable.Builder buildVariable(Variable.Builder builder) {
    return builder;
  }

}

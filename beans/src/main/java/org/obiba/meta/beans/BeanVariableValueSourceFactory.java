/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.meta.beans;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.meta.ValueType;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.VariableValueSourceFactory;
import org.obiba.meta.type.EnumeratedType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.util.Assert;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
    this.propertyNameToVariableName = HashBiMap.create(propertyNameToVariableName);
  }

  public void setOccurrenceGroup(String occurrenceGroup) {
    this.occurrenceGroup = occurrenceGroup;
  }

  public Set<VariableValueSource> createSources(String collection) {
    return doBuildVariables(collection);
  }

  /**
   * Finds the {@code PropertyDescriptor} for a given {@code propertyName} which may denote a nested property (property
   * path). Note that the returned {@code PropertyDescriptor} may be for a different class than the {@code beanClass}
   * (when property is nested).
   * @param propertyName
   * @return
   */
  protected PropertyDescriptor getPropertyDescriptor(String propertyName) {
    Class<?> currentType = getBeanClass();

    String propertyPath = propertyName;
    // Loop as long as the propertyPath designates a nested property
    while(PropertyAccessorUtils.isNestedOrIndexedProperty(propertyPath)) {
      int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);

      // Represents a property of the currentType
      String nestedProperty = propertyPath.substring(0, pos);
      PropertyDescriptor currentProperty = BeanUtils.getPropertyDescriptor(currentType, nestedProperty);
      if(currentProperty == null) {
        throw new IllegalArgumentException("Invalid path '" + propertyName + "' for type " + getBeanClass().getName() + ": nested property '" + nestedProperty + "' does not exist on type " + currentType.getName());
      }
      // Change the current type so it points to the nested type
      currentType = currentProperty.getPropertyType();
      // Extract the nested type's property path from the original path
      propertyPath = propertyPath.substring(pos + 1);
    }
    // propertyPath is a direct reference to a property of the currentType (no longer a path)
    return BeanUtils.getPropertyDescriptor(currentType, propertyPath);
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
  protected Set<VariableValueSource> doBuildVariables(String collection) {
    if(sources == null) {
      synchronized(this) {
        if(sources == null) {
          sources = new HashSet<VariableValueSource>();
          for(String propertyPath : properties) {
            PropertyDescriptor descriptor = getPropertyDescriptor(propertyPath);
            if(descriptor == null) {
              throw new IllegalArgumentException("Invalid property path'" + propertyPath + "' for type " + getBeanClass().getName());
            }
            sources.add(new BeanPropertyVariableValueSource(doBuildVariable(collection, descriptor.getPropertyType(), lookupVariableName(propertyPath)), beanClass, propertyPath));
          }
        }
      }
    }
    return sources;
  }

  protected Variable doBuildVariable(String collection, Class<?> propertyType, String name) {
    ValueType type = ValueType.Factory.forClass(propertyType);

    Variable.Builder builder = Variable.Builder.newVariable(collection, name, type, entityType);
    if(type instanceof EnumeratedType) {
      builder.addCategories(((EnumeratedType) type).enumerate(propertyType));
    }

    if(occurrenceGroup != null) {
      builder.repeatable().occurrenceGroup(occurrenceGroup);
    }
    return buildVariable(builder).build();
  }

  protected Variable.Builder buildVariable(Variable.Builder builder) {
    return builder;
  }

}

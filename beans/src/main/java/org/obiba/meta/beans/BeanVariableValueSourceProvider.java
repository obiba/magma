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

import org.obiba.meta.IVariable;
import org.obiba.meta.IVariableValueSource;
import org.obiba.meta.IVariableValueSourceProvider;
import org.obiba.meta.ValueType;
import org.obiba.meta.type.ValueTypeFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessorUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 *
 */
public class BeanVariableValueSourceProvider implements IVariableValueSourceProvider {

  private Class<?> beanClass;

  /** The set of bean properties that are returned as variables */
  private Set<String> properties = Collections.emptySet();

  /** Maps property names to variable name */
  private BiMap<String, String> propertyNameToVariableName = HashBiMap.create();

  private Set<IVariableValueSource> sources;

  public BeanVariableValueSourceProvider(Class<?> beanClass) {
    this.beanClass = beanClass;
  }

  public void setProperties(Set<String> properties) {
    this.properties = properties;
  }

  public void setPropertyNameToVariableName(Map<String, String> propertyNameToVariableName) {
    this.propertyNameToVariableName = HashBiMap.create(propertyNameToVariableName);
  }

  public Set<IVariableValueSource> getVariables() {
    doBuildVariables();
    return sources;
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
    return name != null ? name : propertyName;
  }

  /**
   * Returns the property name for a given variable name. If no variable is found, this method returns the variable name
   * as-is.
   * @param name
   * @return
   */
  protected String lookupPropertyName(String name) {
    String propertyName = propertyNameToVariableName.inverse().get(name);
    if(propertyName == null) {
      propertyName = name;
    }
    return propertyName;
  }

  private Class<?> getBeanClass() {
    return beanClass;
  }

  /**
   * Builds the {@code IVariable} that this provider supports and also the {@code IVariableEntityDataSource} instances
   * for each variable.
   * @param parent the parent {@code IVariable} of all provided {@code IVariable}
   */
  private void doBuildVariables() {
    if(sources != null) {
      return;
    }
    synchronized(this) {
      if(sources == null) {
        sources = new HashSet<IVariableValueSource>();
        for(String propertyPath : properties) {
          PropertyDescriptor descriptor = getPropertyDescriptor(propertyPath);
          if(descriptor == null) {
            throw new IllegalArgumentException("Invalid property path'" + propertyPath + "' for type " + getBeanClass().getName());
          }
          ValueType type = ValueTypeFactory.INSTANCE.forClass(descriptor.getPropertyType());
          IVariable variable = IVariable.Builder.newVariable(lookupVariableName(propertyPath), type).build();
          sources.add(new BeanPropertyVariableValueSource(variable, propertyPath));
        }
      }
    }
  }

}

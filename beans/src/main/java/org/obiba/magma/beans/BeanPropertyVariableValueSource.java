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

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Connects a {@code Variable} to a bean property.
 */
public class BeanPropertyVariableValueSource implements VariableValueSource {

  private Variable variable;

  private Class<?> beanClass;

  private ValueSetBeanResolver resolver;

  private String propertyPath;

  public BeanPropertyVariableValueSource(Variable variable, Class<?> beanClass, ValueSetBeanResolver resolver, String propertyPath) {
    Assert.notNull(variable, "variable cannot be null");
    Assert.notNull(beanClass, "beanClass cannot be null");
    Assert.notNull(resolver, "resolver cannot be null");
    Assert.notNull(propertyPath, "propertyPath cannot be null");

    this.variable = variable;
    this.beanClass = beanClass;
    this.resolver = resolver;
    this.propertyPath = propertyPath;
  }

  public Variable getVariable() {
    return variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  public Value getValue(ValueSet valueSet) {
    Object bean = resolver.resolve(beanClass, valueSet, variable);

    if(bean == null) {
      return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    }

    if(variable.isRepeatable()) {
      Iterable<?> iterable = (Iterable<?>) bean;
      Iterable<Value> values = Iterables.transform(iterable, new Function<Object, Value>() {
        @Override
        public Value apply(Object bean) {
          Object object = getPropertyValue(propertyPath, PropertyAccessorFactory.forBeanPropertyAccess(bean));
          return getValueType().valueOf(object);
        }
      });
      return getValueType().sequenceOf(values);
    } else {
      Object object = getPropertyValue(propertyPath, PropertyAccessorFactory.forBeanPropertyAccess(bean));
      return getValueType().valueOf(object);
    }
  }

  protected Object getPropertyValue(String propertyPath, BeanWrapper bw) {
    try {
      return bw.getPropertyValue(propertyPath);
    } catch(NullValueInNestedPathException e) {
      return null;
    }
  }
}

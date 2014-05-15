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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.VectorSourceNotSupportedException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Connects a {@code Variable} to a bean property.
 */
public class BeanPropertyVariableValueSource extends AbstractVariableValueSource implements VariableValueSource {

  private final Variable variable;

  private final Class<?> beanClass;

  private final String propertyPath;

  public BeanPropertyVariableValueSource(Variable variable, Class<?> beanClass, String propertyPath) {
    Assert.notNull(variable, "variable cannot be null");
    Assert.notNull(beanClass, "beanClass cannot be null");
    Assert.notNull(propertyPath, "propertyPath cannot be null");

    this.variable = variable;
    this.beanClass = beanClass;
    this.propertyPath = propertyPath;
  }

  @NotNull
  @Override
  public Variable getVariable() {
    return variable;
  }

  @NotNull
  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  @NotNull
  public Value getValue(ValueSet valueSet) {
    Object bean = ((BeanValueSet) valueSet).resolve(beanClass, valueSet, variable);
    if(bean == null) {
      return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    }

    if(variable.isRepeatable()) {
      Iterable<Value> values = Iterables.transform((Iterable<?>) bean, new Function<Object, Value>() {
        @Override
        public Value apply(Object bean) {
          Object object = getPropertyValue(bean);
          return getValueType().valueOf(object);
        }
      });
      return getValueType().sequenceOf(ImmutableList.copyOf(values));
    }

    Object object = getPropertyValue(bean);
    return getValueType().valueOf(object);

  }

  @Override
  public boolean supportVectorSource() {
    return false;
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    throw new VectorSourceNotSupportedException(getClass());
  }

  @Nullable
  protected Object getPropertyValue(Object bean) {
    try {
      return PropertyAccessorFactory.forBeanPropertyAccess(bean).getPropertyValue(propertyPath);
    } catch(NullValueInNestedPathException e) {
      return null;
    } catch(InvalidPropertyException e) {
      throw new MagmaRuntimeException(
          "Invalid definition of variable " + getVariable().getName() + ". Cannot obtain value for property '" +
              e.getPropertyName() + "' on bean of class " + e.getBeanClass(), e);
    }
  }
}

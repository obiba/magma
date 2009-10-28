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

import org.obiba.meta.MetaEngine;
import org.obiba.meta.NoSuchValueSetException;
import org.obiba.meta.Value;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueSetReferenceResolver;
import org.obiba.meta.ValueType;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;

/**
 * Connects a {@code IVariable} to a bean property.
 */
public class BeanPropertyVariableValueSource implements VariableValueSource {

  private ValueSetReferenceResolver resolver;

  private Variable variable;

  private String propertyPath;

  public BeanPropertyVariableValueSource(ValueSetReferenceResolver resolver, Variable variable, String propertyPath) {
    Assert.notNull(resolver, "resolver cannot be null");
    Assert.notNull(variable, "variable cannot be null");
    Assert.notNull(propertyPath, "propertyPath cannot be null");

    this.variable = variable;
    this.propertyPath = propertyPath;
    this.resolver = resolver;
  }

  public Variable getVariable() {
    return variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  public Value getValue(ValueSetReference reference) {
    if(resolver.canResolve(reference) == false) {
      throw new NoSuchValueSetException(reference);
    }
    Object bean = resolver.resolve(reference);
    if(bean == null) {
      // TODO: what's the policy? Throw, return null, return null-value?
      throw new IllegalStateException("resolved bean cannot be null.");
    }
    Object object = getPropertyValue(propertyPath, PropertyAccessorFactory.forBeanPropertyAccess(bean));
    return MetaEngine.get().getValueFactory().newValue(variable.getValueType(), object);
  }

  protected Object getPropertyValue(String propertyPath, BeanWrapper bw) {
    try {
      return bw.getPropertyValue(propertyPath);
    } catch(NullValueInNestedPathException e) {
      return null;
    }
  }
}

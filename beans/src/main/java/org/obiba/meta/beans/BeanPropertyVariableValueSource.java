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

import org.obiba.meta.IValueSetReference;
import org.obiba.meta.IVariable;
import org.obiba.meta.IVariableValueSource;
import org.obiba.meta.Value;
import org.obiba.meta.ValueFactory;
import org.obiba.meta.ValueType;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * Connects a {@code IVariable} to a bean property.
 */
public class BeanPropertyVariableValueSource implements IVariableValueSource {

  private IVariable variable;

  private String propertyPath;

  public BeanPropertyVariableValueSource(IVariable variable, String propertyPath) {
    this.variable = variable;
    this.propertyPath = propertyPath;
  }

  public IVariable getVariable() {
    return variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  public Value getValue(IValueSetReference reference) {
    Object bean = reference.resolve();
    if(bean == null) {
      // TODO: what's the policy? Throw, return null, return null-value?
      throw new IllegalStateException("resolved bean cannot be null.");
    }
    Object object = getPropertyValue(propertyPath, PropertyAccessorFactory.forBeanPropertyAccess(bean));
    return ValueFactory.INSTANCE.newValue(variable.getValueType(), object);
  }

  protected Object getPropertyValue(String propertyPath, BeanWrapper bw) {
    try {
      return bw.getPropertyValue(propertyPath);
    } catch(NullValueInNestedPathException e) {
      return null;
    }
  }
  /*
   * protected Value toData(ValueType type, Object value) { if(value == null) { return new Value(type, null); }
   * if(value.getClass().isEnum()) { value = value.toString(); } if(value instanceof Serializable == false) { throw new
   * IllegalArgumentException(""); } return new Value(type, (Serializable) value); }
   */
}

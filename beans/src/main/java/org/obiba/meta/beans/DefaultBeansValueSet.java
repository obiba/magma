package org.obiba.meta.beans;

import java.util.Set;

import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;

public class DefaultBeansValueSet implements BeansValueSet {

  private ValueSet valueSet;

  private Set<ValueSetBeanResolver> resolvers;

  public DefaultBeansValueSet(ValueSet valueSet, Set<ValueSetBeanResolver> resolvers) {
    this.valueSet = valueSet;
    this.resolvers = resolvers;
  }

  @Override
  public ValueSet getValueSet() {
    return valueSet;
  }

  public Object findBean(Class<?> type, Variable variable) {
    for(ValueSetBeanResolver resolver : resolvers) {
      Object bean = resolver.resolveBean(valueSet, type, variable);
      if(bean != null) {
        return bean;
      }
    }
    throw new NoSuchBeanException("No bean of type " + type.getName() + " in ValueSet " + this + " for variable " + variable + " could be found.");
  }

}

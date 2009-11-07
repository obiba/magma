package org.obiba.meta.beans;

import java.util.Set;

import org.obiba.meta.Variable;
import org.obiba.meta.support.AbstractDatasource;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractBeansDatasource extends AbstractDatasource implements BeansDatasource {

  private Set<ValueSetBeanResolver> beanResolvers;

  @Override
  public void initialise() {
    super.initialise();
    beanResolvers = buildResolvers(new ImmutableSet.Builder<ValueSetBeanResolver>()).build();
  }

  protected abstract ImmutableSet.Builder<ValueSetBeanResolver> buildResolvers(ImmutableSet.Builder<ValueSetBeanResolver> builder);

  @Override
  public <B> B resolveBean(BeanValueSetConnection connection, Class<B> type, Variable variable) {
    for(ValueSetBeanResolver resolver : beanResolvers) {
      B bean = resolver.resolveBean(connection, type, variable);
      if(bean != null) {
        return bean;
      }
    }
    throw new NoSuchBeanException("No bean of type " + type.getName() + " in ValueSet " + connection.getValueSet() + " for variable " + variable + " could be found.");
  }

}

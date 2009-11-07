package org.obiba.meta.beans;

import java.util.Set;

import org.obiba.meta.support.AbstractDatasource;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractBeansDatasource extends AbstractDatasource {

  private Set<ValueSetBeanResolver> beanResolvers;

  @Override
  public void initialise() {
    super.initialise();
    beanResolvers = buildResolvers(new ImmutableSet.Builder<ValueSetBeanResolver>()).build();
  }

  protected abstract ImmutableSet.Builder<ValueSetBeanResolver> buildResolvers(ImmutableSet.Builder<ValueSetBeanResolver> builder);

  protected Set<ValueSetBeanResolver> getBeanResolvers() {
    return beanResolvers;
  }
}

package org.obiba.magma.spring;

import java.util.Set;

/**
 * Provider of {@link ValueTableFactoryBean}s.
 */
public interface ValueTableFactoryBeanProvider {

  public Set<? extends ValueTableFactoryBean> getValueTableFactoryBeans();
}

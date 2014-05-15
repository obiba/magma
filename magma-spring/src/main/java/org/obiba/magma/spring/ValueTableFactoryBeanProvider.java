package org.obiba.magma.spring;

import java.util.Set;

/**
 * Provider of {@link ValueTableFactoryBean}s.
 */
public interface ValueTableFactoryBeanProvider {

  Set<? extends ValueTableFactoryBean> getValueTableFactoryBeans();
}

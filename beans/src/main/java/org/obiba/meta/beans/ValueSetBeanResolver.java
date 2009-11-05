package org.obiba.meta.beans;

import org.obiba.meta.Variable;

public interface ValueSetBeanResolver {

  public <B> B resolveBean(BeanValueSetConnection connection, Class<B> type, Variable variable);

}

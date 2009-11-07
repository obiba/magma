package org.obiba.meta.beans;

import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;

public interface ValueSetBeanResolver {

  public Object resolveBean(ValueSet valueSet, Class<?> type, Variable variable);

}

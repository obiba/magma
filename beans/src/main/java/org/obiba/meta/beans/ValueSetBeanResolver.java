package org.obiba.meta.beans;

import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;

public interface ValueSetBeanResolver {

  public boolean resolves(Class<?> type);

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable);

}

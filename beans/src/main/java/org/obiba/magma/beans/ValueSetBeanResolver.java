package org.obiba.magma.beans;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;

public interface ValueSetBeanResolver {

  public boolean resolves(Class<?> type);

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException;

}

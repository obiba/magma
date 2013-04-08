package org.obiba.magma.beans;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;

public interface ValueSetBeanResolver {

  boolean resolves(Class<?> type);

  Object resolve(Class<?> type, ValueSet valueSet, Variable variable) throws NoSuchBeanException;

}

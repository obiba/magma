package org.obiba.magma.beans;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSetProvider;
import org.obiba.magma.Variable;

public interface BeansValueSetProvider extends ValueSetProvider {

  public Object resolveBean(ValueSet valueSet, Class<?> type, Variable variable);

}

package org.obiba.meta.beans;

import org.obiba.meta.ValueSet;
import org.obiba.meta.ValueSetProvider;
import org.obiba.meta.Variable;

public interface BeansValueSetProvider extends ValueSetProvider {

  public Object resolveBean(ValueSet valueSet, Class<?> type, Variable variable);

}

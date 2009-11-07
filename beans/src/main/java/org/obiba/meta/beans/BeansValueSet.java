package org.obiba.meta.beans;

import org.obiba.meta.ValueSet;
import org.obiba.meta.Variable;

public interface BeansValueSet {

  public ValueSet getValueSet();

  public Object findBean(Class<?> type, Variable variable);

}

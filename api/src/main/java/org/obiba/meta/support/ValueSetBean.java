package org.obiba.meta.support;

import org.obiba.meta.ValueSet;
import org.obiba.meta.ValueSetProvider;
import org.obiba.meta.VariableEntity;

public class ValueSetBean implements ValueSet {

  private ValueSetProvider valueSetProvider;

  private VariableEntity entity;

  public ValueSetBean(ValueSetProvider valueSetProvider, VariableEntity entity) {
    this.valueSetProvider = valueSetProvider;
    this.entity = entity;
  }

  @Override
  public ValueSetProvider getValueSetProvider() {
    return valueSetProvider;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entity;
  }

}

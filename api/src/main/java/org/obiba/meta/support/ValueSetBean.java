package org.obiba.meta.support;

import java.util.Date;

import org.obiba.meta.ValueSet;
import org.obiba.meta.ValueSetProvider;
import org.obiba.meta.VariableEntity;

public class ValueSetBean implements ValueSet {

  private ValueSetProvider valueSetProvider;

  private VariableEntity entity;

  private Date startDate;

  private Date endDate;

  public ValueSetBean(ValueSetProvider valueSetProvider, VariableEntity entity, Date startDate, Date endDate) {
    this.valueSetProvider = valueSetProvider;
    this.entity = entity;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @Override
  public ValueSetProvider getValueSetProvider() {
    return valueSetProvider;
  }

  @Override
  public Date getEndDate() {
    return endDate;
  }

  @Override
  public Date getStartDate() {
    return startDate;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entity;
  }

}

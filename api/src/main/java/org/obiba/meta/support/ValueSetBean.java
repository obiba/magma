package org.obiba.meta.support;

import java.util.Date;

import org.obiba.meta.Collection;
import org.obiba.meta.ValueSet;
import org.obiba.meta.ValueSetAdaptor;
import org.obiba.meta.VariableEntity;

public class ValueSetBean implements ValueSet {

  private Collection collection;

  private VariableEntity entity;

  private Date startDate;

  private Date endDate;

  private ValueSetAdaptor valueSetAdaptor;

  public ValueSetBean(ValueSetAdaptor valueSetAdaptor, Collection collection, VariableEntity entity, Date startDate, Date endDate) {
    this.collection = collection;
    this.entity = entity;
    this.startDate = startDate;
    this.endDate = endDate;
    this.valueSetAdaptor = valueSetAdaptor;
  }

  @Override
  public Collection getCollection() {
    return collection;
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

  @Override
  public <T> T adapt(Class<T> type) {
    return valueSetAdaptor.adapt(type, this);
  }

}

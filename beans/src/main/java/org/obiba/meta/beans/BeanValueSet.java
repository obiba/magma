package org.obiba.meta.beans;

import java.util.Date;

import org.obiba.meta.Collection;
import org.obiba.meta.ValueSet;
import org.obiba.meta.ValueSetExtension;
import org.obiba.meta.VariableEntity;

public class BeanValueSet<T> implements ValueSet {

  private Collection collection;

  private VariableEntity entity;

  private T bean;

  BeanValueSet(Collection collection, VariableEntity entity, T bean) {
    this.collection = collection;
    this.entity = entity;
    this.bean = bean;
  }

  @Override
  public Collection getCollection() {
    return collection;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entity;
  }

  @Override
  public Date getEndDate() {
    return null;
  }

  @Override
  public Date getStartDate() {
    return null;
  }

  public T getBean() {
    return bean;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <E> E extend(String extensionName) {
    return ((ValueSetExtension<BeanValueSet, E>) getCollection().getExtension(extensionName)).extend(this);
  }
}

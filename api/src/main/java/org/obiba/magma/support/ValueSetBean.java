package org.obiba.magma.support;

import org.obiba.magma.Collection;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;

public class ValueSetBean implements ValueSet {

  private Collection collection;

  private VariableEntity entity;

  public ValueSetBean(Collection collection, VariableEntity entity) {
    this.collection = collection;
    this.entity = entity;
  }

  protected ValueSetBean(ValueSet valueSet) {
    this.collection = valueSet.getCollection();
    this.entity = valueSet.getVariableEntity();
  }

  @Override
  public Collection getCollection() {
    return collection;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entity;
  }

}

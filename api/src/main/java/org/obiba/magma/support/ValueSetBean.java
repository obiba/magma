package org.obiba.magma.support;

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

public class ValueSetBean implements ValueSet {

  private final ValueTable table;

  private final VariableEntity entity;

  public ValueSetBean(ValueTable table, VariableEntity entity) {
    if(table == null) throw new IllegalArgumentException("table cannot be null");
    if(entity == null) throw new IllegalArgumentException("entity cannot be null");
    this.table = table;
    this.entity = entity;
  }

  protected ValueSetBean(ValueSet valueSet) {
    this(valueSet.getValueTable(), valueSet.getVariableEntity());
  }

  @Override
  public Timestamps getTimestamps() {
    return getValueTable().getTimestamps();
  }

  @Override
  public ValueTable getValueTable() {
    return table;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entity;
  }

  @Override
  public String toString() {
    return "valueSet[" + getValueTable() + ":" + getVariableEntity() + "]";
  }

}

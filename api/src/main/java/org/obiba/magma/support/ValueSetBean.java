package org.obiba.magma.support;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

public class ValueSetBean implements ValueSet {

  private ValueTable table;

  private VariableEntity entity;

  public ValueSetBean(ValueTable table, VariableEntity entity) {
    this.table = table;
    this.entity = entity;
  }

  protected ValueSetBean(ValueSet valueSet) {
    this.table = valueSet.getValueTable();
    this.entity = valueSet.getVariableEntity();
  }

  @Override
  public ValueTable getValueTable() {
    return table;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return entity;
  }

}

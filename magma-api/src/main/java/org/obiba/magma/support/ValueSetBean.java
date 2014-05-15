package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

public class ValueSetBean implements ValueSet {

  @NotNull
  private final ValueTable table;

  @NotNull
  private final VariableEntity entity;

  @SuppressWarnings("ConstantConditions")
  public ValueSetBean(@NotNull ValueTable table, @NotNull VariableEntity entity) {
    if(table == null) throw new IllegalArgumentException("table cannot be null");
    if(entity == null) throw new IllegalArgumentException("entity cannot be null");
    this.table = table;
    this.entity = entity;
  }

  protected ValueSetBean(ValueSet valueSet) {
    this(valueSet.getValueTable(), valueSet.getVariableEntity());
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return getValueTable().getTimestamps();
  }

  @Override
  @NotNull
  public ValueTable getValueTable() {
    return table;
  }

  @Override
  @NotNull
  public VariableEntity getVariableEntity() {
    return entity;
  }

  @Override
  public String toString() {
    return "valueSet[" + getValueTable() + ":" + getVariableEntity() + "]";
  }

}

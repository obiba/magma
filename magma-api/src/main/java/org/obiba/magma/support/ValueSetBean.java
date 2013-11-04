package org.obiba.magma.support;

import javax.annotation.Nonnull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

public class ValueSetBean implements ValueSet {

  @Nonnull
  private final ValueTable table;

  @Nonnull
  private final VariableEntity entity;

  @SuppressWarnings("ConstantConditions")
  public ValueSetBean(@Nonnull ValueTable table, @Nonnull VariableEntity entity) {
    if(table == null) throw new IllegalArgumentException("table cannot be null");
    if(entity == null) throw new IllegalArgumentException("entity cannot be null");
    this.table = table;
    this.entity = entity;
  }

  protected ValueSetBean(ValueSet valueSet) {
    this(valueSet.getValueTable(), valueSet.getVariableEntity());
  }

  @Nonnull
  @Override
  public Timestamps getTimestamps() {
    return getValueTable().getTimestamps();
  }

  @Override
  @Nonnull
  public ValueTable getValueTable() {
    return table;
  }

  @Override
  @Nonnull
  public VariableEntity getVariableEntity() {
    return entity;
  }

  @Override
  public String toString() {
    return "valueSet[" + getValueTable() + ":" + getVariableEntity() + "]";
  }

}

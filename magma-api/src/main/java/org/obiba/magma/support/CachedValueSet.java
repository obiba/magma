package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.springframework.cache.Cache;

public class CachedValueSet implements ValueSet {
  private ValueSet wrapped;
  private Cache cache;
  private ValueTable table;
  private VariableEntity entity;

  public CachedValueSet(@NotNull ValueTable table, @NotNull VariableEntity entity, @NotNull ValueSet wrapped, @NotNull Cache cache) {
    this.table = table;
    this.entity = entity;
    this.wrapped = wrapped;
    this.cache = cache;
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
  public Timestamps getTimestamps() {
    return new CachedTimestamps(getWrapped().getTimestamps(), this, cache);
  }

  public ValueSet getWrapped() {
    return wrapped;
  }
}

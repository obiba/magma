package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.springframework.cache.Cache;

public class CachedValueSet implements ValueSet {
  private ValueSet wrapped;
  private Cache cache;
  private CachedValueTable table;
  private VariableEntity variableEntity;

  public CachedValueSet(@NotNull CachedValueTable table, @NotNull VariableEntity variableEntity, @NotNull Cache cache) {
    this.table = table;
    this.variableEntity = variableEntity;
    this.cache = cache;

    try{
      this.wrapped = table.getWrappedValueTable().getValueSet(variableEntity);
    } catch(MagmaRuntimeException ex) {
      //ignore
    }
  }

  @Override
  public ValueTable getValueTable() {
    return table;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return variableEntity;
  }

  @Override
  public Timestamps getTimestamps() {
    return new CachedTimestamps(this, cache);
  }

  public ValueSet getWrapped() {
    if (this.wrapped == null)
      throw new MagmaRuntimeException("wrapped value not initialized.");

    return wrapped;
  }
}

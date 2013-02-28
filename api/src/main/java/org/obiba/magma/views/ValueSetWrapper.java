/**
 *
 */
package org.obiba.magma.views;

import javax.annotation.Nonnull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.TransformingValueTable;

public class ValueSetWrapper implements ValueSet {

  @Nonnull
  private final TransformingValueTable mappingTable;

  @Nonnull
  private final ValueSet wrapped;

  ValueSetWrapper(@Nonnull TransformingValueTable mappingTable, @Nonnull ValueSet wrapped) {
    this.mappingTable = mappingTable;
    this.wrapped = wrapped;
  }

  @Override
  public ValueTable getValueTable() {
    return mappingTable;
  }

  @Override
  public VariableEntity getVariableEntity() {
    return mappingTable.getVariableEntityMappingFunction().apply(wrapped.getVariableEntity());
  }

  public ValueSet getWrappedValueSet() {
    return wrapped;
  }

  @Override
  public Timestamps getTimestamps() {
    return wrapped.getTimestamps();
  }
}
package org.obiba.magma.transform;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

public interface TransformingValueTable extends ValueTable {

  @NotNull
  BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction();

  @NotNull
  BijectiveFunction<ValueSet, ValueSet> getValueSetMappingFunction();

  @NotNull
  BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction();

}

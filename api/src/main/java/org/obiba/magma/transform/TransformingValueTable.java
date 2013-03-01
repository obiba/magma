package org.obiba.magma.transform;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

public interface TransformingValueTable extends ValueTable {

  BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction();

  BijectiveFunction<ValueSet, ValueSet> getValueSetMappingFunction();

  BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction();

}

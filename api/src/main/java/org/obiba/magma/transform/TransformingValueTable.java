package org.obiba.magma.transform;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

public interface TransformingValueTable extends ValueTable {

  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction();

  public BijectiveFunction<ValueSet, ValueSet> getValueSetMappingFunction();

  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction();

}

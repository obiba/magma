package org.obiba.magma;

import java.util.Set;

public interface ValueTable extends Timestamped {

  String getName();

  Datasource getDatasource();

  String getEntityType();

  boolean isForEntityType(String entityType);

  Set<VariableEntity> getVariableEntities();

  boolean hasValueSet(VariableEntity entity);

  Iterable<ValueSet> getValueSets();

  ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException;

  boolean hasVariable(String name);

  Iterable<Variable> getVariables();

  Variable getVariable(String name) throws NoSuchVariableException;

  Value getValue(Variable variable, ValueSet valueSet);

  VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException;

  boolean isView();

}

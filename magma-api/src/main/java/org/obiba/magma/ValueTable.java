package org.obiba.magma;

import java.util.Set;

import javax.validation.constraints.NotNull;

public interface ValueTable extends Timestamped {

  @NotNull
  String getName();

  @NotNull
  Datasource getDatasource();

  String getEntityType();

  boolean isForEntityType(String entityType);

  Set<VariableEntity> getVariableEntities();

  boolean hasValueSet(VariableEntity entity);

  Iterable<ValueSet> getValueSets();

  ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException;

  Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException;

  boolean hasVariable(String name);

  Iterable<Variable> getVariables();

  Variable getVariable(String name) throws NoSuchVariableException;

  Value getValue(Variable variable, ValueSet valueSet);

  VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException;

  boolean isView();

  String getTableReference();
}

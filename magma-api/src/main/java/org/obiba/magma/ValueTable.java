package org.obiba.magma;

import java.util.Set;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

public interface ValueTable extends Timestamped {

  @NotNull
  String getName();

  @NotNull
  Datasource getDatasource();

  String getEntityType();

  boolean isForEntityType(String entityType);

  Set<VariableEntity> getVariableEntities();

  int getVariableEntityCount();

  boolean hasValueSet(VariableEntity entity);

  Iterable<ValueSet> getValueSets();

  int getValueSetCount();

  ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException;

  boolean canDropValueSets();

  void dropValueSets();

  Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException;

  Iterable<Timestamps> getValueSetTimestamps(SortedSet<VariableEntity> entities);

  boolean hasVariable(String name);

  Iterable<Variable> getVariables();

  int getVariableCount();

  Variable getVariable(String name) throws NoSuchVariableException;

  Value getValue(Variable variable, ValueSet valueSet);

  VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException;

  boolean isView();

  String getTableReference();

  class Reference {

    private Reference() {}

    public static String getReference(String datasource, String table) {
      return datasource + "." + table;
    }

  }

}

package org.obiba.magma.support;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractValueTableWrapper implements ValueTableWrapper {

  @Override
  public abstract ValueTable getWrappedValueTable();

  @NotNull
  @Override
  public Datasource getDatasource() {
    return getWrappedValueTable().getDatasource();
  }

  @Override
  public String getEntityType() {
    return getWrappedValueTable().getEntityType();
  }

  @NotNull
  @Override
  public String getName() {
    return getWrappedValueTable().getName();
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    return getWrappedValueTable().getValue(variable, valueSet);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return getWrappedValueTable().getVariableEntities();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return getWrappedValueTable().getValueSet(entity);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getWrappedValueTable().getValueSetTimestamps(entity);
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(SortedSet<VariableEntity> entities) {
    return getWrappedValueTable().getValueSetTimestamps(entities);
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return getWrappedValueTable().getValueSets();
  }

  @Override
  public boolean hasVariable(String name) {
    return getWrappedValueTable().hasVariable(name);
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    return getWrappedValueTable().getVariable(name);
  }

  @Override
  public Iterable<Variable> getVariables() {
    return getWrappedValueTable().getVariables();
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    return getWrappedValueTable().getVariableValueSource(variableName);
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return getWrappedValueTable().hasValueSet(entity);
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getWrappedValueTable().isForEntityType(entityType);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return getWrappedValueTable().getTimestamps();
  }

  @Override
  public boolean isView() {
    return getWrappedValueTable().isView();
  }

  @Override
  public ValueTable getInnermostWrappedValueTable() {
    return getWrappedValueTable() instanceof ValueTableWrapper //
        ? ((ValueTableWrapper) getWrappedValueTable()).getInnermostWrappedValueTable() //
        : getWrappedValueTable();
  }

  @Override
  public String getTableReference() {
    return getWrappedValueTable().getTableReference();
  }

  @Override
  public int getVariableCount() {
    return getWrappedValueTable().getVariableCount();
  }

  @Override
  public int getValueSetCount() {
    return getWrappedValueTable().getValueSetCount();
  }

  @Override
  public int getVariableEntityCount() {
    return getWrappedValueTable().getVariableEntityCount();
  }
}

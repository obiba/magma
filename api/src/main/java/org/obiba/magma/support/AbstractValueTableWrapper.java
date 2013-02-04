package org.obiba.magma.support;

import java.util.Set;

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

public abstract class AbstractValueTableWrapper implements ValueTableWrapper {

  @Override
  public abstract ValueTable getWrappedValueTable();

  @Override
  public Datasource getDatasource() {
    return getWrappedValueTable().getDatasource();
  }

  @Override
  public String getEntityType() {
    return getWrappedValueTable().getEntityType();
  }

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
  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    return getWrappedValueTable().getVariableValueSource(name);
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return getWrappedValueTable().hasValueSet(entity);
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getWrappedValueTable().isForEntityType(entityType);
  }

  @Override
  public Timestamps getTimestamps() {
    return getWrappedValueTable().getTimestamps();
  }

  @Override
  public boolean isView() {
    return getWrappedValueTable().isView();
  }

}

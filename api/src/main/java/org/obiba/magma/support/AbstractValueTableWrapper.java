package org.obiba.magma.support;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

public abstract class AbstractValueTableWrapper implements ValueTableWrapper {

  public abstract ValueTable getWrappedValueTable();

  public Datasource getDatasource() {
    return getWrappedValueTable().getDatasource();
  }

  public String getEntityType() {
    return getWrappedValueTable().getEntityType();
  }

  public String getName() {
    return getWrappedValueTable().getName();
  }

  public Value getValue(Variable variable, ValueSet valueSet) {
    return getWrappedValueTable().getValue(variable, valueSet);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return getWrappedValueTable().getVariableEntities();
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return getWrappedValueTable().getValueSet(entity);
  }

  public Iterable<ValueSet> getValueSets() {
    return getWrappedValueTable().getValueSets();
  }

  @Override
  public boolean hasVariable(String name) {
    return getWrappedValueTable().hasVariable(name);
  }

  public Variable getVariable(String name) throws NoSuchVariableException {
    return getWrappedValueTable().getVariable(name);
  }

  public Iterable<Variable> getVariables() {
    return getWrappedValueTable().getVariables();
  }

  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    return getWrappedValueTable().getVariableValueSource(name);
  }

  public boolean hasValueSet(VariableEntity entity) {
    return getWrappedValueTable().hasValueSet(entity);
  }

  public boolean isForEntityType(String entityType) {
    return getWrappedValueTable().isForEntityType(entityType);
  }
}

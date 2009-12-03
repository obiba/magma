package org.obiba.magma.support;

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
  private ValueTable wrapped;

  protected AbstractValueTableWrapper(ValueTable wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return wrapped;
  }

  public Datasource getDatasource() {
    return wrapped.getDatasource();
  }

  public String getEntityType() {
    return wrapped.getEntityType();
  }

  public String getName() {
    return wrapped.getName();
  }

  public Value getValue(Variable variable, ValueSet valueSet) {
    return wrapped.getValue(variable, valueSet);
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return wrapped.getValueSet(entity);
  }

  public Iterable<ValueSet> getValueSets() {
    return wrapped.getValueSets();
  }

  public Variable getVariable(String name) throws NoSuchVariableException {
    return wrapped.getVariable(name);
  }

  public Iterable<Variable> getVariables() {
    return wrapped.getVariables();
  }

  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    return wrapped.getVariableValueSource(name);
  }

  public boolean hasValueSet(VariableEntity entity) {
    return wrapped.hasValueSet(entity);
  }

  public boolean isForEntityType(String entityType) {
    return wrapped.isForEntityType(entityType);
  }
}

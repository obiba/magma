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

public class NullValueTable implements ValueTable {
  //
  // Constants
  //

  private static final NullValueTable INSTANCE = new NullValueTable();

  //
  // Constructors
  //

  private NullValueTable() {
  }

  //
  // ValueTable Methods
  //

  public String getName() {
    return null;
  }

  public Datasource getDatasource() {
    return null;
  }

  public String getEntityType() {
    return null;
  }

  public boolean isForEntityType(String entityType) {
    return false;
  }

  public Set<VariableEntity> getVariableEntities() {
    return null;
  }

  public boolean hasValueSet(VariableEntity entity) {
    return false;
  }

  public Iterable<ValueSet> getValueSets() {
    return null;
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return null;
  }

  public boolean hasVariable(String name) {
    return false;
  }

  public Iterable<Variable> getVariables() {
    return null;
  }

  public Variable getVariable(String name) throws NoSuchVariableException {
    return null;
  }

  public Value getValue(Variable variable, ValueSet valueSet) {
    return null;
  }

  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    return null;
  }

  public Timestamps getTimestamps(ValueSet valueSet) {
    return null;
  }

  //
  // Methods
  //

  public static NullValueTable getInstance() {
    return INSTANCE;
  }
}
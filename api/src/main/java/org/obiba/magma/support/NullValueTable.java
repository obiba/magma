package org.obiba.magma.support;

import java.lang.ref.WeakReference;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class NullValueTable implements ValueTable {
  //
  // Constants
  //

  private static WeakReference<NullValueTable> instance = MagmaEngine.get().registerInstance(new NullValueTable());

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
    return "";
  }

  public boolean isForEntityType(String entityType) {
    return false;
  }

  public Set<VariableEntity> getVariableEntities() {
    return ImmutableSet.of();
  }

  public boolean hasValueSet(VariableEntity entity) {
    return false;
  }

  public Iterable<ValueSet> getValueSets() {
    return ImmutableList.of();
  }

  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    throw new NoSuchValueSetException(this, entity);
  }

  public boolean hasVariable(String name) {
    return false;
  }

  public Iterable<Variable> getVariables() {
    return null;
  }

  public Variable getVariable(String name) throws NoSuchVariableException {
    throw new NoSuchVariableException("null", name);
  }

  public Value getValue(Variable variable, ValueSet valueSet) {
    return null;
  }

  public VariableValueSource getVariableValueSource(String name) throws NoSuchVariableException {
    throw new NoSuchVariableException("null", name);
  }

  public Timestamps getTimestamps(ValueSet valueSet) {
    return NullTimestamps.get();
  }

  //
  // Methods
  //

  public static NullValueTable get() {
    if(instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new NullValueTable());
    }
    return instance.get();
  }

  @Override
  public boolean isView() {
    return false;
  }
}
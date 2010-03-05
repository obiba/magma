/**
 * 
 */
package org.obiba.magma.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

public class JoinTable implements ValueTable {
  //
  // Instance Variables
  //

  private List<ValueTable> tables;

  /**
   * Cached set of all variables of all tables in the join (i.e., the union).
   */
  private Set<Variable> unionOfVariables;

  /**
   * Cached map of variable names to tables.
   */
  private Map<String, ValueTable> variableNameToTableMap;

  //
  // Constructors
  //

  /**
   * No-arg constructor (mainly for XStream).
   */
  public JoinTable() {
    super();
  }

  public JoinTable(List<ValueTable> tables) {
    if(tables == null) {
      throw new IllegalArgumentException("null tables");
    }
    if(tables.size() < 2) {
      throw new IllegalArgumentException("tables must have two or more members");
    }

    this.tables = new ArrayList<ValueTable>();
    if(tables != null) {
      this.tables.addAll(tables);
    }
  }

  //
  // ValueTable Methods
  //

  @Override
  public Datasource getDatasource() {
    // TODO: A JoinTable does not belong to a Datasource (or does it? which one?).
    return null;
  }

  @Override
  public String getEntityType() {
    return tables.get(0).getEntityType();
  }

  @Override
  public String getName() {
    return buildJoinTableName();
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    ValueTable vt = getFirstTableWithVariable(variable.getName());
    if(vt != null) {
      return vt.getValue(variable, valueSet);
    } else {
      throw new NoSuchVariableException(variable.getName());
    }
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    for(ValueSet valueSet : getValueSets()) {
      if(valueSet.getVariableEntity().getIdentifier().equals(entity.getIdentifier())) {
        return valueSet;
      }
    }
    throw new NoSuchValueSetException(this, entity);
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    Set<ValueSet> valueSets = new LinkedHashSet<ValueSet>();

    for(ValueSet valueSet : tables.get(0).getValueSets()) {
      if(isInAllTables(valueSet)) {
        valueSets.add(valueSet);
      }
    }

    return valueSets;
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    for(Variable variable : getVariables()) {
      if(variable.getName().equals(name)) {
        return variable;
      }
    }

    throw new NoSuchVariableException(name);
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    ValueTable vt = getFirstTableWithVariable(variableName);
    if(vt != null) {
      return vt.getVariableValueSource(variableName);
    } else {
      throw new NoSuchVariableException(variableName);
    }
  }

  @Override
  public Iterable<Variable> getVariables() {
    return unionOfVariables();
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    for(ValueSet valueSet : getValueSets()) {
      if(valueSet.getVariableEntity().getIdentifier().equals(entity.getIdentifier())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getEntityType().equals(entityType);
  }

  //
  // Methods
  //

  private String buildJoinTableName() {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < tables.size(); i++) {
      sb.append(tables.get(i));
      if(i < tables.size() - 1) {
        sb.append('-');
      }
    }
    return sb.toString();
  }

  private Set<Variable> unionOfVariables() {
    if(unionOfVariables == null) {
      unionOfVariables = new LinkedHashSet<Variable>();

      Set<String> unionOfVariableNames = new LinkedHashSet<String>();

      for(ValueTable vt : tables) {
        for(Variable variable : vt.getVariables()) {
          if(!unionOfVariableNames.contains(variable.getName())) {
            unionOfVariableNames.add(variable.getName());
            unionOfVariables.add(variable);
          }
        }
      }
    }

    return unionOfVariables;
  }

  private boolean isInAllTables(ValueSet valueSet) {
    boolean presentInAllTables = true;
    for(int i = 1; i < tables.size(); i++) {
      ValueTable vt = tables.get(i);
      if(!vt.hasValueSet(valueSet.getVariableEntity())) {
        presentInAllTables = false;
        break;
      }
    }
    return presentInAllTables;
  }

  private ValueTable getFirstTableWithVariable(String variableName) {
    if(variableNameToTableMap == null) {
      variableNameToTableMap = new HashMap<String, ValueTable>();
    }

    ValueTable cachedTable = variableNameToTableMap.get(variableName);
    if(cachedTable != null) {
      return cachedTable;
    } else {
      for(ValueTable vt : tables) {
        for(Variable variable : vt.getVariables()) {
          if(variable.getName().equals(variableName)) {
            variableNameToTableMap.put(variableName, vt);
            return vt;
          }
        }
      }
    }

    return null;
  }
}
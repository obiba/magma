/**
 * 
 */
package org.obiba.magma.views;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.ValueSetBean;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class JoinTable implements ValueTable, Initialisable {
  //
  // Instance Variables
  //

  private List<ValueTable> tables;

  /**
   * Cached set of all variables of all tables in the join (i.e., the union).
   */
  private transient Set<Variable> unionOfVariables;

  /**
   * Cached map of variable names to tables.
   */
  private transient Map<String, ValueTable> variableNameToTableMap;

  // An arbitrary number to initialise the LinkedHashSet with a capacity close to the actual value (see
  // getVariableEntities())
  private transient int lastEntityCount = 5000;

  //
  // Constructors
  //

  /**
   * No-arg constructor (mainly for XStream).
   */
  public JoinTable() {
    super();
  }

  public JoinTable(List<ValueTable> tables, boolean validateEntityTypes) {
    if(tables == null) {
      throw new IllegalArgumentException("null tables");
    }
    if(tables.size() < 2) {
      throw new IllegalArgumentException("tables must have two or more members");
    }
    if(validateEntityTypes) {
      String entityType = tables.get(0).getEntityType();
      for(int i = 1; i < tables.size(); i++) {
        if(!tables.get(i).isForEntityType(entityType)) {
          throw new IllegalArgumentException("tables must all have the same entity type");
        }
      }
    }

    this.tables = ImmutableList.copyOf(tables);
  }

  public JoinTable(List<ValueTable> tables) {
    this(tables, true);
  }

  public List<ValueTable> getTables() {
    return ImmutableList.copyOf(tables);
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
    ValueTable valueTable = getFirstTableWithVariable(variable.getName());
    if(valueTable == null) throw new NoSuchVariableException(variable.getName());
    ValueSet vs = valueSet;
    if(valueSet instanceof JoinedValueSet) {
      vs = ((JoinedValueSet) valueSet).getInnerTableValueSet(valueTable);
    }
    return valueTable.getValue(variable, vs);
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if(hasValueSet(entity)) {
      return new JoinedValueSet(this, entity);
    }
    throw new NoSuchValueSetException(this, entity);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    // Set the initial capacity to the number of entities we saw in the previous call to this method
    Set<VariableEntity> entities = new LinkedHashSet<VariableEntity>(lastEntityCount);
    for(ValueTable table : tables) {
      entities.addAll(table.getVariableEntities());
    }
    // Remember this value so that next time around, the set is initialised with a capacity closer to the actual value.
    lastEntityCount = entities.size();
    return entities;
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return Iterables.transform(getVariableEntities(), new Function<VariableEntity, ValueSet>() {

      @Override
      public ValueSet apply(VariableEntity from) {
        return new JoinedValueSet(JoinTable.this, from);
      }

    });
  }

  @Override
  public boolean hasVariable(String name) {
    return getFirstTableWithVariable(name) != null;
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
      return new JoinedVariableValueSource(vt, vt.getVariableValueSource(variableName));
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
    for(ValueTable table : tables) {
      if(table.hasValueSet(entity)) {
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
  // Initialisable Methods
  //

  public void initialise() {
    for(ValueTable vt : tables) {
      if(vt instanceof Initialisable) {
        ((Initialisable) vt).initialise();
      }
    }
  }

  //
  // Methods
  //

  private String buildJoinTableName() {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < tables.size(); i++) {
      sb.append(tables.get(i).getName());
      if(i < tables.size() - 1) {
        sb.append('-');
      }
    }
    return sb.toString();
  }

  private synchronized Set<Variable> unionOfVariables() {
    if(unionOfVariables == null) {
      unionOfVariables = new LinkedHashSet<Variable>();

      Set<String> unionOfVariableNames = new LinkedHashSet<String>();

      for(ValueTable vt : tables) {
        for(Variable variable : vt.getVariables()) {
          // Add returns true if the set did not already contain the value
          if(unionOfVariableNames.add(variable.getName())) {
            unionOfVariables.add(variable);
          }
        }
      }
    }

    return unionOfVariables;
  }

  private synchronized ValueTable getFirstTableWithVariable(String variableName) {
    if(variableNameToTableMap == null) {
      variableNameToTableMap = new HashMap<String, ValueTable>();
    }

    ValueTable cachedTable = variableNameToTableMap.get(variableName);
    if(cachedTable != null) {
      return cachedTable;
    } else {
      for(ValueTable vt : tables) {
        if(vt.hasVariable(variableName)) {
          variableNameToTableMap.put(variableName, vt);
          return vt;
        }
      }
    }

    return null;
  }

  //
  // Inner Classes
  //

  static class JoinedValueSet extends ValueSetBean {

    private final Map<String, ValueSet> valueSets = Maps.newHashMap();

    JoinedValueSet(ValueTable table, VariableEntity entity) {
      super(table, entity);
    }

    synchronized ValueSet getInnerTableValueSet(ValueTable valueTable) {
      ValueSet valueSet = null;
      if(valueTable.hasValueSet(getVariableEntity())) {
        valueSet = valueSets.get(valueTable.getName());
        if(valueSet == null) {
          valueSet = valueTable.getValueSet(getVariableEntity());
          valueSets.put(valueTable.getName(), valueSet);
        }
      }
      return valueSet;
    }
  }

  private static class JoinedVariableValueSource implements VariableValueSource {

    private final ValueTable owner;

    private final VariableValueSource wrapped;

    private JoinedVariableValueSource(ValueTable owner, VariableValueSource wrapped) {
      this.owner = owner;
      this.wrapped = wrapped;
    }

    @Override
    public Variable getVariable() {
      return wrapped.getVariable();
    }

    @Override
    public Value getValue(ValueSet valueSet) {
      JoinedValueSet joined = (JoinedValueSet) valueSet;
      ValueSet joinedSet = joined.getInnerTableValueSet(owner);
      if(joinedSet != null) {
        return wrapped.getValue(joinedSet);
      }
      return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    }

    @Override
    public ValueType getValueType() {
      return wrapped.getValueType();
    }

    @Override
    public VectorSource asVectorSource() {
      return wrapped.asVectorSource();
    }

    @Override
    public boolean equals(Object that) {
      if(this == that) return true;
      if(that instanceof JoinedVariableValueSource) {
        JoinedVariableValueSource jvvs = (JoinedVariableValueSource) that;
        return this.owner.equals(jvvs.owner) && this.wrapped.equals(jvvs.wrapped);
      }
      return super.equals(that);
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 37 * result + owner.hashCode();
      result = 37 * result + wrapped.hashCode();
      return result;
    }
  }

  @Override
  public Timestamps getTimestamps(ValueSet valueSet) {
    return new JoinTimestamps(valueSet, tables);
  }
}
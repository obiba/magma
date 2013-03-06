/**
 *
 */
package org.obiba.magma.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import org.obiba.magma.support.AbstractVariableValueSourceWrapper;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.support.ValueSetBean;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

@SuppressWarnings({ "UnusedDeclaration", "TransientFieldInNonSerializableClass" })
public class JoinTable implements ValueTable, Initialisable {

  private static final int DEFAULT_ENTITY_COUNT = 5000;

  @Nonnull
  private final List<ValueTable> tables;

  /**
   * Cached set of all variables of all tables in the join (i.e., the union).
   */
  private transient Set<Variable> unionOfVariables;

  /**
   * Cached map of variable names to tables.
   */
  @Nullable
  private transient Multimap<JoinableVariable, ValueTable> variableTables;

  /**
   * Map first found JoinableVariable by its name
   */
  @Nonnull
  private transient final Map<String, JoinableVariable> joinableVariablesByName = Maps.newHashMap();

  // An arbitrary number to initialise the LinkedHashSet with a capacity close to the actual value
  // See getVariableEntities()
  private transient int lastEntityCount = DEFAULT_ENTITY_COUNT;

  /**
   * No-arg constructor (mainly for XStream).
   */
  public JoinTable() {
    this(new ArrayList<ValueTable>());
  }

  public JoinTable(@Nonnull List<ValueTable> tables) {
    this(tables, true);
  }

  @SuppressWarnings("ConstantConditions")
  public JoinTable(@Nonnull List<ValueTable> tables, boolean validateEntityTypes) {
    if(tables == null) throw new IllegalArgumentException("null tables");
    if(tables.isEmpty()) throw new IllegalArgumentException("empty tables");

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

  @Nonnull
  private Multimap<JoinableVariable, ValueTable> getVariableTables() {
    if(variableTables == null) {
      variableTables = ArrayListMultimap.create();
      analyseVariables();
    }
    return variableTables;
  }

  private synchronized void analyseVariables() {
    for(ValueTable table : tables) {
      for(Variable variable : table.getVariables()) {
        JoinableVariable joinableVariable = new JoinableVariable(variable);
        getVariableTables().put(joinableVariable, table);
        joinableVariablesByName.put(variable.getName(), joinableVariable);
      }
    }
  }

  @Nonnull
  public List<ValueTable> getTables() {
    return tables;
  }

  @Override
  public Datasource getDatasource() {
    // A JoinTable does not belong to a Datasource (or does it? which one?).
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
    return getVariableValueSource(variable.getName()).getValue(valueSet);
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if(hasValueSet(entity)) {
      return new JoinedValueSet(this, entity);
    }
    throw new NoSuchValueSetException(this, entity);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getValueSet(entity).getTimestamps();
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
    return joinableVariablesByName.containsKey(name);
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
    // find first variable with this name
    JoinableVariable joinableVariable = joinableVariablesByName.get(variableName);
    if(joinableVariable == null) {
      throw new NoSuchVariableException(variableName);
    }
    List<ValueTable> tablesWithVariable = getTablesWithVariable(joinableVariable);
    ValueTable table = Iterables.getFirst(tablesWithVariable, null);
    if(table == null) {
      throw new NoSuchVariableException(variableName);
    }
    return new JoinedVariableValueSource(tablesWithVariable, table.getVariableValueSource(variableName));
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

  @Override
  public void initialise() {
    for(ValueTable table : tables) {
      if(table instanceof Initialisable) {
        ((Initialisable) table).initialise();
      }
    }
  }

  private String buildJoinTableName() {
    StringBuilder sb = new StringBuilder();
    for(Iterator<ValueTable> it = tables.iterator(); it.hasNext(); ) {
      sb.append(it.next().getName());
      if(it.hasNext()) sb.append('-');
    }
    return sb.toString();
  }

  private synchronized Iterable<Variable> unionOfVariables() {
    if(unionOfVariables == null) {
      unionOfVariables = new LinkedHashSet<Variable>();

      Collection<String> unionOfVariableNames = new LinkedHashSet<String>();
      for(ValueTable table : tables) {
        for(Variable variable : table.getVariables()) {
          // Add returns true if the set did not already contain the value
          if(unionOfVariableNames.add(variable.getName())) {
            unionOfVariables.add(variable);
          }
        }
      }
    }

    return unionOfVariables;
  }

  @Nonnull
  private synchronized List<ValueTable> getTablesWithVariable(Variable variable) throws NoSuchVariableException {
    return getTablesWithVariable(new JoinableVariable(variable));
  }

  @Nonnull
  private synchronized List<ValueTable> getTablesWithVariable(@Nonnull JoinableVariable joinableVariable)
      throws NoSuchVariableException {

    Collection<ValueTable> cachedTables = getVariableTables().get(joinableVariable);
    if(cachedTables == null) {
      throw new NoSuchVariableException(joinableVariable.getName());
    }
    List<ValueTable> filteredList = ImmutableList.copyOf(Iterables.filter(cachedTables, Predicates.notNull()));
    if(filteredList.isEmpty()) {
      throw new NoSuchVariableException(joinableVariable.getName());
    }
    return filteredList;
  }

  @Override
  public Timestamps getTimestamps() {
    return new UnionTimestamps(tables);
  }

  @Override
  public boolean isView() {
    return false;
  }

  static class JoinedValueSet extends ValueSetBean {

    @Nonnull
    private final Map<String, ValueSet> valueSetsByTable = Maps.newHashMap();

    JoinedValueSet(@Nonnull ValueTable table, @Nonnull VariableEntity entity) {
      super(table, entity);
    }

    @Override
    public Timestamps getTimestamps() {
      return new UnionTimestamps(valueSetsByTable.values());
    }

    synchronized Iterable<ValueSet> getInnerTableValueSets(Iterable<ValueTable> valueTables) {
      List<ValueSet> valueSets = Lists.newArrayList();
      for(ValueTable valueTable : valueTables) {
        if(valueSetsByTable.containsKey(valueTable.getName())) {
          ValueSet valueSet = valueSetsByTable.get(valueTable.getName());
          if(valueSet != null) valueSets.add(valueSet);
        } else if(valueTable.hasValueSet(getVariableEntity())) {
          ValueSet valueSet = valueTable.getValueSet(getVariableEntity());
          valueSetsByTable.put(valueTable.getName(), valueSet);
          valueSets.add(valueSet);
        }
      }
      return valueSets;
    }
  }

  private static class JoinedVariableValueSource extends AbstractVariableValueSourceWrapper {

    @Nonnull
    private final List<ValueTable> owners;

    private JoinedVariableValueSource(@Nonnull List<ValueTable> owners, @Nonnull VariableValueSource wrapped) {
      super(wrapped);
      this.owners = owners;
    }

    @Nonnull
    @Override
    public Value getValue(ValueSet valueSet) {
      for(ValueSet joinedValueSet : ((JoinedValueSet) valueSet).getInnerTableValueSets(owners)) {
        Value value = getWrapped().getValue(joinedValueSet);
        if(!value.isNull()) return value;
      }
      return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    }

    @Override
    public boolean equals(Object that) {
      if(this == that) return true;
      if(that instanceof JoinedVariableValueSource) {
        JoinedVariableValueSource jvvs = (JoinedVariableValueSource) that;
        return getWrapped().equals(jvvs.getWrapped()) && Iterables.elementsEqual(owners, jvvs.owners);
      }
      return super.equals(that);
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 37 * result + owners.hashCode();
      result = 37 * result + getWrapped().hashCode();
      return result;
    }

  }

  private static class JoinableVariable {

    @Nonnull
    private final String name;

    @Nonnull
    private final ValueType valueType;

    private final boolean repeatable;

    private JoinableVariable(@Nonnull String name, @Nonnull ValueType valueType, boolean repeatable) {
      this.name = name;
      this.valueType = valueType;
      this.repeatable = repeatable;
    }

    private JoinableVariable(@Nonnull Variable variable) {
      this(variable.getName(), variable.getValueType(), variable.isRepeatable());
    }

    @Nonnull
    public String getName() {
      return name;
    }

    @Nonnull
    public ValueType getValueType() {
      return valueType;
    }

    public boolean isRepeatable() {
      return repeatable;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name, valueType, repeatable);
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null || getClass() != obj.getClass()) {
        return false;
      }
      JoinableVariable other = (JoinableVariable) obj;
      return Objects.equal(name, other.name) && Objects.equal(valueType, other.valueType) &&
          repeatable == other.repeatable;
    }
  }
}
/**
 *
 */
package org.obiba.magma.views;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.Orderings;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.support.ValueSetBean;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
  private final transient Multimap<JoinableVariable, ValueTable> variableTables = HashMultimap.create();

  // An arbitrary number to initialise the LinkedHashSet with a capacity close to the actual value
  // See getVariableEntities()
  private transient int lastEntityCount = DEFAULT_ENTITY_COUNT;

  /**
   * No-arg constructor (mainly for XStream).
   */
  public JoinTable() {
    tables = ImmutableList.of();
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

  public JoinTable(@Nonnull List<ValueTable> tables) {
    this(tables, true);
  }

  @Nonnull
  public List<ValueTable> getTables() {
    return tables;
  }

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
    return findFirstJoinableVariable(name) != null;
  }

  @Nullable
  private JoinableVariable findFirstJoinableVariable(final String name) {
    try {
      return Iterables.find(variableTables.keys(), new Predicate<JoinableVariable>() {
        @Override
        public boolean apply(@Nullable JoinableVariable variable) {
          return variable != null && Objects.equal(variable.getName(), name);
        }
      });
    } catch(NoSuchElementException e) {
      return null;
    }
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
    JoinableVariable joinableVariable = findFirstJoinableVariable(variableName);
    if(joinableVariable == null) {
      throw new NoSuchVariableException(variableName);
    }
    Set<ValueTable> tablesWithVariable = getTablesWithVariable(joinableVariable);
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
  private synchronized Set<ValueTable> getTablesWithVariable(Variable variable) {
    return getTablesWithVariable(new JoinableVariable(variable));
  }

  @Nonnull
  private synchronized Set<ValueTable> getTablesWithVariable(@Nonnull JoinableVariable joinableVariable)
      throws NoSuchVariableException {
    if(variableTables.containsKey(joinableVariable)) {
      Collection<ValueTable> cachedTables = variableTables.get(joinableVariable);
      if(cachedTables == null) {
        throw new NoSuchVariableException(joinableVariable.getName());
      }
      Set<ValueTable> filteredSet = ImmutableSet.copyOf(Iterables.filter(cachedTables, Predicates.notNull()));
      if(filteredSet.isEmpty()) {
        throw new NoSuchVariableException(joinableVariable.getName());
      }
      return filteredSet;
    }
    for(ValueTable table : tables) {
      JoinableVariable tableVariable = null;
      try {
        tableVariable = new JoinableVariable(table.getVariable(joinableVariable.getName()));
      } catch(NoSuchVariableException ignored) {
      }
      variableTables.put(joinableVariable, Objects.equal(joinableVariable, tableVariable) ? table : null);
    }
    return getTablesWithVariable(joinableVariable);
  }

  @Override
  public Timestamps getTimestamps() {
    return new UnionTimestamps(tables);
  }

  @Override
  public boolean isView() {
    return false;
  }

  private static class JoinedValueSet extends ValueSetBean {

    private final Map<String, ValueSet> valueSetsByTable = Maps.newHashMap();

    private JoinedValueSet(@Nonnull ValueTable table, @Nonnull VariableEntity entity) {
      super(table, entity);
    }

    @Override
    public Timestamps getTimestamps() {
      return new UnionTimestamps(valueSetsByTable.values());
    }

    synchronized ValueSet getInnerTableValueSet(Iterable<ValueTable> valueTables) {
      ValueSet valueSet = null;
      for(ValueTable valueTable : valueTables) {
        if(valueTable.hasValueSet(getVariableEntity())) {
          valueSet = valueSetsByTable.get(valueTable.getName());
          if(valueSet == null) {
            valueSet = valueTable.getValueSet(getVariableEntity());
            valueSetsByTable.put(valueTable.getName(), valueSet);
          }
        }
      }
      return valueSet;
    }
  }

  private static class JoinedVariableValueSource implements VariableValueSource {

    @Nonnull
    private final List<ValueTable> owners;

    @Nonnull
    private final VariableValueSource wrapped;

    private JoinedVariableValueSource(@Nonnull Iterable<ValueTable> owners, @Nonnull VariableValueSource wrapped) {
      this.owners = Orderings.VALUE_TABLE_NAME_ORDERING.sortedCopy(owners);
      this.wrapped = wrapped;
    }

    @Override
    public Variable getVariable() {
      return wrapped.getVariable();
    }

    @Nonnull
    @Override
    public Value getValue(ValueSet valueSet) {
      JoinedValueSet joined = (JoinedValueSet) valueSet;
      ValueSet joinedSet = joined.getInnerTableValueSet(owners);
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
        return Iterables.elementsEqual(owners, jvvs.owners) && wrapped.equals(jvvs.wrapped);
      }
      return super.equals(that);
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 37 * result + owners.hashCode();
      result = 37 * result + wrapped.hashCode();
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
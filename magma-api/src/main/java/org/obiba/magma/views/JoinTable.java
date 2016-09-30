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
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

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
import org.obiba.magma.support.AbstractVariableValueSourceWrapper;
import org.obiba.magma.support.NullTimestamps;
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

  @NotNull
  private final List<ValueTable> tables;

  /**
   * Cached set of all variables of all tables in the join (i.e., the union).
   */
  private transient Set<Variable> unionOfVariables;

  /**
   * Cached map of variable names to tables.
   */
  @NotNull
  private transient final Multimap<JoinableVariable, ValueTable> variableTables = ArrayListMultimap.create();

  /**
   * Map of variable value sources.
   */
  @NotNull
  private transient final Map<String, JoinedVariableValueSource> variableValueSourceMap = Maps.newHashMap();

  /**
   * Map first found JoinableVariable by its name
   */
  @NotNull
  private transient final Map<String, JoinableVariable> joinableVariablesByName = Maps.newHashMap();

  // An arbitrary number to initialise the LinkedHashSet with a capacity close to the actual value
  // See getVariableEntities()
  private transient int lastEntityCount = DEFAULT_ENTITY_COUNT;

  private transient boolean variableAnalysed = false;

  /**
   * No-arg constructor (mainly for XStream).
   */
  public JoinTable() {
    this(new ArrayList<ValueTable>());
  }

  public JoinTable(@NotNull List<ValueTable> tables) {
    this(tables, true);
  }

  @SuppressWarnings("ConstantConditions")
  public JoinTable(@NotNull List<ValueTable> tables, boolean validateEntityTypes) {
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
    //analyseVariables();
  }

  @NotNull
  private Multimap<JoinableVariable, ValueTable> getVariableTables() {
    if(!variableAnalysed) analyseVariables();
    return variableTables;
  }

  @NotNull
  public Map<String, JoinableVariable> getJoinableVariablesByName() {
    if(!variableAnalysed) analyseVariables();
    return joinableVariablesByName;
  }

  public synchronized void analyseVariables() {
    if(variableAnalysed) return;
    for(ValueTable table : tables) {
      for(Variable variable : table.getVariables()) {
        JoinableVariable joinableVariable = new JoinableVariable(variable);
        JoinableVariable existing = joinableVariablesByName.get(variable.getName());
        if(existing != null && !existing.equals(joinableVariable)) {
          throw new IllegalArgumentException(
              "Cannot have variables with same name and different value type or repeatability: '" +
                  buildJoinTableName() + "." + variable.getName() + "'"
          );
        }
        variableTables.put(joinableVariable, table);
        joinableVariablesByName.put(variable.getName(), joinableVariable);
      }
    }
    variableAnalysed = true;
  }

  @NotNull
  public List<ValueTable> getTables() {
    // don't analyse variables here as it is called very often
    return tables;
  }

  @NotNull
  @Override
  @SuppressWarnings({ "NullableProblems", "ConstantConditions" })
  @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NONNULL_RETURN_VIOLATION")
  public Datasource getDatasource() {
    // A JoinTable does not belong to a Datasource (or does it? which one?).
    return null;
  }

  @Override
  public String getEntityType() {
    return getTables().get(0).getEntityType();
  }

  @NotNull
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
  public boolean canDropValueSets() {
    for (ValueTable table : tables) {
      if (!table.canDropValueSets()) return false;
    }
    return true;
  }

  @Override
  public void dropValueSets() {
    for (ValueTable table : tables) {
      table.dropValueSets();
    }
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getValueSet(entity).getTimestamps();
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(final SortedSet<VariableEntity> entities) {
    return new Iterable<Timestamps>() {
      @Override
      public Iterator<Timestamps> iterator() {
        return new TimestampsIterator(entities, getTables());
      }
    };
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    if(!variableAnalysed) analyseVariables();

    // Set the initial capacity to the number of entities we saw in the previous call to this method
    Set<VariableEntity> entities = new LinkedHashSet<>(lastEntityCount);
    for(ValueTable table : getTables()) {
      entities.addAll(table.getVariableEntities());
    }
    // Remember this value so that next time around, the set is initialised with a capacity closer to the actual value.
    lastEntityCount = entities.size();
    return entities;
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return getValueSets(getVariableEntities());
  }

  @Override
  public Iterable<ValueSet> getValueSets(Set<VariableEntity> entities) {
    return Iterables.transform(entities, new Function<VariableEntity, ValueSet>() {
      @Override
      public ValueSet apply(VariableEntity from) {
        return new JoinedValueSet(JoinTable.this, from);
      }
    });
  }

  @Override
  public boolean hasVariable(String name) {
    return getJoinableVariablesByName().containsKey(name);
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
    if(!variableAnalysed) analyseVariables();

    if(!variableValueSourceMap.containsKey(variableName)) {
      // find first variable with this name
      JoinableVariable joinableVariable = getJoinableVariablesByName().get(variableName);
      if(joinableVariable == null) {
        throw new NoSuchVariableException(variableName);
      }
      List<ValueTable> tablesWithVariable = getTablesWithVariable(joinableVariable);
      ValueTable table = Iterables.getFirst(tablesWithVariable, null);
      if(table == null) {
        throw new NoSuchVariableException(variableName);
      }
      variableValueSourceMap.put(variableName,
          new JoinedVariableValueSource(variableName, tablesWithVariable, table.getVariableValueSource(variableName)));
    }

    return variableValueSourceMap.get(variableName);
  }

  @Override
  public Iterable<Variable> getVariables() {
    if(!variableAnalysed) analyseVariables();
    return unionOfVariables();
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    if(!variableAnalysed) analyseVariables();

    for(ValueTable table : getTables()) {
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
    for(Iterator<ValueTable> it = getTables().iterator(); it.hasNext(); ) {
      sb.append(it.next().getName());
      if(it.hasNext()) sb.append('-');
    }
    return sb.toString();
  }

  private synchronized Iterable<Variable> unionOfVariables() {
    if(unionOfVariables == null) {
      unionOfVariables = new LinkedHashSet<>();

      if(!variableAnalysed) analyseVariables();

      Collection<String> unionOfVariableNames = new LinkedHashSet<>();
      for(ValueTable table : getTables()) {
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

  @NotNull
  private synchronized List<ValueTable> getTablesWithVariable(@NotNull JoinableVariable joinableVariable)
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

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new UnionTimestamps(getTables());
  }

  @Override
  public boolean isView() {
    return false;
  }

  @Override
  public String getTableReference() {
    // A JoinTable does not belong to a Datasource (or does it? which one?).
    return "";
  }

  @Override
  public int getVariableCount() {
    return Iterables.size(getVariables());
  }

  @Override
  public int getValueSetCount() {
    return Iterables.size(getValueSets());
  }

  @Override
  public int getVariableEntityCount() {
    return Iterables.size(getVariableEntities());
  }

  static class JoinedValueSet extends ValueSetBean {

    @NotNull
    private final Map<String, ValueSet> valueSetsByTable = Maps.newHashMap();

    @NotNull
    private final Map<String, Timestamps> timestampsByTable = Maps.newHashMap();

    JoinedValueSet(@NotNull ValueTable table, @NotNull VariableEntity entity) {
      super(table, entity);
    }

    @NotNull
    @Override
    public Timestamps getTimestamps() {
      List<Timestamps> timestampses = Lists.newArrayList();
      for(ValueTable valueTable : ((JoinTable) getValueTable()).getTables()) {
        if(valueTable.hasValueSet(getVariableEntity())) {
          timestampses.add(valueTable.getValueSetTimestamps(getVariableEntity()));
        }
      }
      return new UnionTimestamps(timestampses);
    }

    synchronized Iterable<ValueSet> getInnerTableValueSets(Iterable<ValueTable> valueTables) {
      List<ValueSet> valueSets = Lists.newArrayList();
      for(ValueTable valueTable : valueTables) {
        if(valueSetsByTable.containsKey(valueTable.getTableReference())) {
          ValueSet valueSet = valueSetsByTable.get(valueTable.getTableReference());
          if(valueSet != null) valueSets.add(valueSet);
        } else if(valueTable.hasValueSet(getVariableEntity())) {
          ValueSet valueSet = valueTable.getValueSet(getVariableEntity());
          valueSetsByTable.put(valueTable.getTableReference(), valueSet);
          valueSets.add(valueSet);
        }
      }
      return valueSets;
    }
  }

  private static class JoinedVariableValueSource extends AbstractVariableValueSourceWrapper implements VectorSource {

    @NotNull
    private final List<ValueTable> owners;

    @NotNull
    private final String variableName;

    private JoinedVariableValueSource(@NotNull String variableName, @NotNull List<ValueTable> owners,
        @NotNull VariableValueSource wrapped) {
      super(wrapped);
      this.variableName = variableName;
      this.owners = owners;
    }

    private VariableValueSource getWrapped(ValueTable table) {
      return table.getVariableValueSource(variableName);
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      // get inner value sets
      for(ValueSet joinedValueSet : ((JoinedValueSet) valueSet).getInnerTableValueSets(owners)) {
        Value value = getWrapped(joinedValueSet.getValueTable()).getValue(joinedValueSet);
        if(!value.isNull()) return value;
      }
      return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    }

    @Override
    public boolean supportVectorSource() {
      for(ValueTable table : owners) {
        if(!table.getVariableValueSource(variableName).supportVectorSource()) {
          return false;
        }
      }
      return true;
    }

    @Override
    public VectorSource asVectorSource() {
      return this;
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

    @Override
    public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
      return new Iterable<Value>() {
        @Override
        public Iterator<Value> iterator() {
          return new ValueIterator(entities, owners, getVariable());
        }
      };
    }

  }

  private static class ValueIterator implements Iterator<Value> {

    @NotNull
    private final SortedSet<VariableEntity> entities;

    private final Iterator<VariableEntity> entitiesIterator;

    @NotNull
    private final List<ValueTable> owners;

    @NotNull
    private final Variable variable;

    private List<Iterator<Value>> valueIterators = Lists.newArrayList();

    private ValueIterator(SortedSet<VariableEntity> entities, List<ValueTable> owners, Variable variable) {
      this.entities = entities;
      this.owners = owners;
      this.variable = variable;
      entitiesIterator = entities.iterator();
    }

    @Override
    public boolean hasNext() {
      return entitiesIterator.hasNext();
    }

    @Override
    public Value next() {
      // get the value iterator for each table
      if(valueIterators.isEmpty()) {
        for(ValueTable table : owners) {
          VectorSource vSource = table.getVariableValueSource(variable.getName()).asVectorSource();
          valueIterators.add(vSource.getValues(entities).iterator());
        }
      }

      // increment each value iterators and find first not null value
      entitiesIterator.next();
      Value joinedValue = null;
      for(Iterator<Value> vector : valueIterators) {
        Value value = vector.next();
        if(joinedValue == null && !value.isNull()) {
          joinedValue = value;
        }
      }
      if(joinedValue == null) {
        joinedValue = variable.isRepeatable()
            ? variable.getValueType().nullSequence()
            : variable.getValueType().nullValue();
      }

      return joinedValue;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static class TimestampsIterator implements Iterator<Timestamps> {

    @NotNull
    private final SortedSet<VariableEntity> entities;

    private final Iterator<VariableEntity> entitiesIterator;

    @NotNull
    private final List<ValueTable> owners;

    private List<Iterator<Timestamps>> timestampsIterators = Lists.newArrayList();

    private TimestampsIterator(SortedSet<VariableEntity> entities, List<ValueTable> owners) {
      this.entities = entities;
      this.owners = owners;
      entitiesIterator = entities.iterator();
    }

    @Override
    public boolean hasNext() {
      return entitiesIterator.hasNext();
    }

    @Override
    public Timestamps next() {
      // get the value iterator for each table
      if(timestampsIterators.isEmpty()) {
        for(ValueTable table : owners) {
          timestampsIterators.add(table.getValueSetTimestamps(entities).iterator());
        }
      }

      // increment each timestamps iterator and make a union of them
      entitiesIterator.next();
      ImmutableList.Builder<Timestamps> timestamps = ImmutableList.builder();
      for(Iterator<Timestamps> iterator : timestampsIterators) {
        Timestamps ts = iterator.next();
        timestamps.add(ts == null ? NullTimestamps.get() : ts);
      }
      return new UnionTimestamps(timestamps.build());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static class JoinableVariable {

    @NotNull
    private final String name;

    @NotNull
    private final ValueType valueType;

    private final boolean repeatable;

    private JoinableVariable(@NotNull String name, @NotNull ValueType valueType, boolean repeatable) {
      this.name = name;
      this.valueType = valueType;
      this.repeatable = repeatable;
    }

    private JoinableVariable(@NotNull Variable variable) {
      this(variable.getName(), variable.getValueType(), variable.isRepeatable());
    }

    @NotNull
    public String getName() {
      return name;
    }

    @NotNull
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

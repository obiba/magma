/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 */
package org.obiba.magma.views;

import com.google.common.collect.*;
import org.obiba.magma.*;
import org.obiba.magma.support.UnionTimestamps;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"UnusedDeclaration", "TransientFieldInNonSerializableClass"})
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
  private transient final Multimap<Variable, ValueTable> variableTables = ArrayListMultimap.create();

  /**
   * Map of variable value sources.
   */
  @NotNull
  private transient final Map<String, JoinVariableValueSource> variableValueSourceMap = Maps.newHashMap();

  /**
   * Map first found JoinableVariable by its name
   */
  @NotNull
  private transient final Map<String, Variable> joinableVariablesByName = Maps.newHashMap();

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
    if (tables == null) throw new IllegalArgumentException("null tables");
    if (tables.isEmpty()) throw new IllegalArgumentException("empty tables");

    if (validateEntityTypes) {
      String entityType = tables.get(0).getEntityType();
      for (int i = 1; i < tables.size(); i++) {
        if (!tables.get(i).isForEntityType(entityType)) {
          throw new IllegalArgumentException("tables must all have the same entity type");
        }
      }
    }
    this.tables = ImmutableList.copyOf(tables);
    //analyseVariables();
  }

  @NotNull
  private Multimap<Variable, ValueTable> getVariableTables() {
    if (!variableAnalysed) analyseVariables();
    return variableTables;
  }

  @NotNull
  public Map<String, Variable> getJoinableVariablesByName() {
    if (!variableAnalysed) analyseVariables();
    return joinableVariablesByName;
  }

  public synchronized void analyseVariables() {
    if (variableAnalysed) return;
    tables.forEach(table ->
        table.getVariables().forEach(variable -> {

          Variable joinableVariable = VariableBean.Builder.newVariable(variable.getName(), variable.getValueType(), variable.getEntityType()) //
              .repeatable(variable.isRepeatable()).build();
          Variable existing = joinableVariablesByName.get(variable.getName());
          if (existing != null && !existing.equals(joinableVariable)) {
            throw new IllegalArgumentException(
                "Cannot have variables with same name and different value type or repeatability: '" +
                    buildJoinTableName() + "." + variable.getName() + "'"
            );
          }
          variableTables.put(joinableVariable, table);
          joinableVariablesByName.put(variable.getName(), joinableVariable);
        })
    );
    variableAnalysed = true;
  }

  @NotNull
  public List<ValueTable> getTables() {
    // don't analyse variables here as it is called very often
    return tables;
  }

  @NotNull
  @Override
  @SuppressWarnings({"NullableProblems", "ConstantConditions"})
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
    if (hasValueSet(entity)) {
      return new JoinValueSet(this, entity);
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
    tables.parallelStream().forEach(ValueTable::dropValueSets);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getValueSet(entity).getTimestamps();
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(final SortedSet<VariableEntity> entities) {
    return () -> new JoinTimestampsIterator(JoinTable.this, entities);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    if (!variableAnalysed) analyseVariables();

    // Set the initial capacity to the number of entities we saw in the previous call to this method
    Set<VariableEntity> entities = Collections.synchronizedSet(Sets.newLinkedHashSetWithExpectedSize(lastEntityCount));
    getTables().parallelStream().forEach(table -> entities.addAll(table.getVariableEntities()));
    // Remember this value so that next time around, the set is initialised with a capacity closer to the actual value.
    lastEntityCount = entities.size();
    return entities;
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return getValueSets(getVariableEntities());
  }

  @Override
  public Iterable<ValueSet> getValueSets(Iterable<VariableEntity> entities) {
    return () -> new ValueSetIterator(entities);
  }

  @Override
  public boolean hasVariable(String name) {
    return getJoinableVariablesByName().containsKey(name);
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    for (Variable variable : getVariables()) {
      if (variable.getName().equals(name)) {
        return variable;
      }
    }
    throw new NoSuchVariableException(name);
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    if (!variableAnalysed) analyseVariables();

    if (!variableValueSourceMap.containsKey(variableName)) {
      // find first variable with this name
      Variable joinableVariable = getJoinableVariablesByName().get(variableName);
      if (joinableVariable == null) {
        throw new NoSuchVariableException(variableName);
      }
      List<ValueTable> tablesWithVariable = getTablesWithVariable(joinableVariable);
      ValueTable table = Iterables.getFirst(tablesWithVariable, null);
      if (table == null) {
        throw new NoSuchVariableException(variableName);
      }
      variableValueSourceMap.put(variableName,
          new JoinVariableValueSource(joinableVariable, tablesWithVariable));
    }

    return variableValueSourceMap.get(variableName);
  }

  @Override
  public Iterable<Variable> getVariables() {
    if (!variableAnalysed) analyseVariables();
    return unionOfVariables();
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    if (!variableAnalysed) analyseVariables();

    for (ValueTable table : getTables()) {
      if (table.hasValueSet(entity)) {
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
    for (ValueTable table : tables) {
      if (table instanceof Initialisable) {
        ((Initialisable) table).initialise();
      }
    }
  }

  private String buildJoinTableName() {
    StringBuilder sb = new StringBuilder();
    for (Iterator<ValueTable> it = getTables().iterator(); it.hasNext(); ) {
      sb.append(it.next().getName());
      if (it.hasNext()) sb.append('-');
    }
    return sb.toString();
  }

  private synchronized Iterable<Variable> unionOfVariables() {
    if (unionOfVariables == null) {
      unionOfVariables = new LinkedHashSet<>();

      if (!variableAnalysed) analyseVariables();

      Collection<String> unionOfVariableNames = new LinkedHashSet<>();
      for (ValueTable table : getTables()) {
        for (Variable variable : table.getVariables()) {
          // Add returns true if the set did not already contain the value
          if (unionOfVariableNames.add(variable.getName())) {
            unionOfVariables.add(variable);
          }
        }
      }
    }

    return unionOfVariables;
  }

  @NotNull
  private synchronized List<ValueTable> getTablesWithVariable(@NotNull Variable joinableVariable)
      throws NoSuchVariableException {
    Collection<ValueTable> cachedTables = getVariableTables().get(joinableVariable);
    if (cachedTables == null) {
      throw new NoSuchVariableException(joinableVariable.getName());
    }
    List<ValueTable> filteredList = cachedTables.stream().filter(t -> t != null).collect(Collectors.toList());
    if (filteredList.isEmpty()) {
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

  private class ValueSetIterator implements Iterator<ValueSet> {

    private final Iterator<List<VariableEntity>> partitions;

    private Iterator<ValueSet> currentBatch;

    public ValueSetIterator(Iterable<VariableEntity> entities) {
      this.partitions = Iterables.partition(entities, ValueTable.ENTITY_BATCH_SIZE).iterator();
    }

    @Override
    public boolean hasNext() {
      synchronized (partitions) {
        return partitions.hasNext() || (currentBatch != null && currentBatch.hasNext());
      }
    }

    @Override
    public ValueSet next() {
      synchronized (partitions) {
        if (currentBatch == null || !currentBatch.hasNext()) {
          currentBatch = getValueSetsBatch(partitions.next()).getValueSets().iterator();
        }
        return currentBatch.next();
      }
    }

    private ValueSetBatch getValueSetsBatch(final List<VariableEntity> entities) {
      return new JoinValueSetBatch(JoinTable.this, entities);
    }
  }
}

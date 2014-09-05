package org.obiba.magma;

import java.util.Set;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

/**
 * A table has {@link org.obiba.magma.Variable}s (columns) and {@link org.obiba.magma.ValueSet}s (rows).
 * Each {@link org.obiba.magma.ValueSet} is identified by a {@link org.obiba.magma.VariableEntity}.
 * There is a {@link org.obiba.magma.Value} for each table's {@link org.obiba.magma.Variable} and
 * {@link org.obiba.magma.ValueSet}.
 */
public interface ValueTable extends Timestamped, AttributeAware {

  /**
   * Required name that identifies this table, unique in its {@link org.obiba.magma.Datasource}.
   *
   * @return
   */
  @NotNull
  String getName();

  /**
   * Container of this table.
   *
   * @return
   */
  @NotNull
  Datasource getDatasource();

  /**
   * What this table is about ({@link org.obiba.magma.Variable}s must be about the same entity type).
   *
   * @return
   */
  String getEntityType();

  /**
   * Verifies the entity type.
   *
   * @param entityType
   * @return
   */
  boolean isForEntityType(String entityType);

  /**
   * Identifiers of the table {@link org.obiba.magma.ValueSet}s (rows).
   *
   * @return
   */
  Set<VariableEntity> getVariableEntities();

  /**
   * The count of {@link org.obiba.magma.VariableEntity}s (implementations can query such a number).
   *
   * @return
   */
  int getVariableEntityCount();

  /**
   * Verifies that the given {@link org.obiba.magma.VariableEntity} has a {@link org.obiba.magma.ValueSet} (row) in this table.
   *
   * @param entity
   * @return
   */
  boolean hasValueSet(VariableEntity entity);

  /**
   * Get all {@link org.obiba.magma.ValueSet}s.
   *
   * @return
   */
  Iterable<ValueSet> getValueSets();

  /**
   * Get the count of {@link org.obiba.magma.ValueSet}s (implementations can query such a number).
   *
   * @return
   */
  int getValueSetCount();

  /**
   * Get the {@link org.obiba.magma.ValueSet} (row) for the given {@link org.obiba.magma.VariableEntity}.
   *
   * @param entity
   * @return
   * @throws NoSuchValueSetException
   * @see #hasValueSet(VariableEntity)
   */
  ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException;

  /**
   * Check if {@link org.obiba.magma.ValueSet} (row) deletion is supported.
   *
   * @return
   */
  boolean canDropValueSets();

  /**
   * Delete all {@link org.obiba.magma.ValueSet}s (rows) of this table.
   *
   * @see #canDropValueSets()
   */
  void dropValueSets();

  /**
   * Get the timestamp of the {@link org.obiba.magma.ValueSet} (row) identified by
   * its {@link org.obiba.magma.VariableEntity}.
   *
   * @param entity
   * @return
   * @throws NoSuchValueSetException
   * @see #hasValueSet(VariableEntity)
   */
  Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException;

  /**
   * Get the timestamps of the {@link org.obiba.magma.ValueSet}s (rows) identified by
   * their {@link org.obiba.magma.VariableEntity}s.
   *
   * @param entities
   * @return
   */
  Iterable<Timestamps> getValueSetTimestamps(SortedSet<VariableEntity> entities);

  /**
   * Check there is a {@link org.obiba.magma.Variable} (column) with the given name.
   *
   * @param name
   * @return
   */
  boolean hasVariable(String name);

  /**
   * Get all {@link org.obiba.magma.Variable}s.
   *
   * @return
   */
  Iterable<Variable> getVariables();

  /**
   * Get the count of {@link Variable}s (implementations can query such a number).
   *
   * @return
   */
  int getVariableCount();

  /**
   * Get the {@link org.obiba.magma.Variable} (column) with the given name.
   *
   * @param name
   * @return
   * @throws NoSuchVariableException
   * @see #hasVariable(String)
   */
  Variable getVariable(String name) throws NoSuchVariableException;

  /**
   * Get the {@link org.obiba.magma.Value} for a given {@link org.obiba.magma.Variable} (column)
   * and a given {@link org.obiba.magma.ValueSet} (row).
   *
   * @param variable
   * @param valueSet
   * @return
   */
  Value getValue(Variable variable, ValueSet valueSet);

  /**
   * Get the {@link org.obiba.magma.VariableValueSource} (column accessor) for the given variable name.
   * @param variableName
   * @return
   * @throws NoSuchVariableException
   * @see #hasVariable(String)
   */
  VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException;

  /**
   * whether this table is a logical table (a view) or a raw one (implemented over a persistence layer).
   * @return
   */
  boolean isView();

  /**
   * The unique name of this table, using the schema "datasource_name:table_name".
   * @return
   */
  String getTableReference();

  /**
   * Table reference helper.
   */
  class Reference {

    private Reference() {}

    public static String getReference(String datasource, String table) {
      return datasource + "." + table;
    }

  }

}

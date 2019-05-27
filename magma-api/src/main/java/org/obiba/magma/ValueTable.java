/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Represent the dataset and gives access to the variables and the entities.
 */
public interface ValueTable extends Timestamped {

  int ENTITY_BATCH_SIZE = 100;

  /**
   * Name of the table.
   *
   * @return
   */
  @NotNull
  String getName();

  /**
   * Parent datasource.
   *
   * @return
   */
  @NotNull
  Datasource getDatasource();

  /**
   * What this table is about.
   *
   * @return
   */
  String getEntityType();

  /**
   * Compare with table's entity type.
   *
   * @param entityType
   * @return
   */
  boolean isForEntityType(String entityType);

  /**
   * Get the entities objects in a list (no ordering applied, order must be consistent
   * so that client can query by offset-limit.
   *
   * @return
   */
  List<VariableEntity> getVariableEntities();

  /**
   * Get the a variable entities page. Default implementation makes a sublist of the entity list, a specific implementation
   * would query the entities from the storage.
   *
   * @param offset from the start if < 0
   * @param limit  until the end if < 0
   * @return
   */
  default List<VariableEntity> getVariableEntities(int offset, int limit) {
    int total = getVariableEntityCount();
    int from = Math.max(offset, 0);
    from = Math.min(from, total);
    int to = limit >= 0 ? from + limit : total;
    to = Math.min(to, total);
    return getVariableEntities().subList(from, to);
  }

  /**
   * Summary of entities.
   *
   * @return
   */
  int getVariableEntityCount();

  /**
   * Check if this entity exists.
   *
   * @param entity
   * @return
   */
  boolean hasValueSet(VariableEntity entity);

  /**
   * Iterate over the value sets.
   *
   * @return
   */
  Iterable<ValueSet> getValueSets();

  /**
   * Iterate over the values sets for a set of entities.
   *
   * @param entities
   * @return
   */
  Iterable<ValueSet> getValueSets(Iterable<VariableEntity> entities);

  /**
   * Summary of value sets.
   *
   * @return
   */
  default int getValueSetCount() {
    return getVariableEntityCount();
  }

  /**
   * Get a specific value set.
   *
   * @param entity
   * @return
   * @throws NoSuchValueSetException
   */
  ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException;

  /**
   * Whether the table implementation supports value set dropping.
   *
   * @return
   */
  boolean canDropValueSets();

  /**
   * Drop all value sets, similar to truncate operation in SQL.
   */
  void dropValueSets();

  /**
   * Get the timestamps of a value set.
   *
   * @param entity
   * @return
   * @throws NoSuchValueSetException
   */
  Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException;

  /**
   * Iterate over the timestamps of value set's entities.
   *
   * @param entities
   * @return
   */
  Iterable<Timestamps> getValueSetTimestamps(List<VariableEntity> entities);

  /**
   * Check table has variable (name is unique in a table).
   *
   * @param name
   * @return
   */
  boolean hasVariable(String name);

  /**
   * Iterate over all variables.
   *
   * @return
   */
  Iterable<Variable> getVariables();

  /**
   * Summary about variables.
   *
   * @return
   */
  int getVariableCount();

  /**
   * Get a specific variable (name is unique in a table).
   *
   * @param name
   * @return
   * @throws NoSuchVariableException
   */
  Variable getVariable(String name) throws NoSuchVariableException;

  /**
   * Get a variable value in a value set.
   *
   * @param variable
   * @param valueSet
   * @return
   */
  Value getValue(Variable variable, ValueSet valueSet);

  /**
   * Get the wrapper object that gives access to values.
   *
   * @param variableName
   * @return
   * @throws NoSuchVariableException
   */
  VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException;

  /**
   * Check it is a logical table.
   *
   * @return
   */
  boolean isView();

  /**
   * Get the unique name of the table.
   *
   * @return
   */
  String getTableReference();

  /**
   * Get the preferred size for the batch of entities when reading the value sets by chuncks.
   *
   * @return
   */
  default int getVariableEntityBatchSize() {
    return ENTITY_BATCH_SIZE;
  }

  class Reference {

    private Reference() {
    }

    public static String getReference(String datasource, String table) {
      return datasource + "." + table;
    }

  }

}

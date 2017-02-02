/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import java.util.Set;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

public interface ValueTable extends Timestamped {

  int ENTITY_BATCH_SIZE = 100;

  @NotNull
  String getName();

  @NotNull
  Datasource getDatasource();

  String getEntityType();

  boolean isForEntityType(String entityType);

  Set<VariableEntity> getVariableEntities();

  int getVariableEntityCount();

  boolean hasValueSet(VariableEntity entity);

  Iterable<ValueSet> getValueSets();

  Iterable<ValueSet> getValueSets(Iterable<VariableEntity> entities);

  int getValueSetCount();

  ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException;

  boolean canDropValueSets();

  void dropValueSets();

  Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException;

  Iterable<Timestamps> getValueSetTimestamps(SortedSet<VariableEntity> entities);

  boolean hasVariable(String name);

  Iterable<Variable> getVariables();

  int getVariableCount();

  Variable getVariable(String name) throws NoSuchVariableException;

  Value getValue(Variable variable, ValueSet valueSet);

  VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException;

  boolean isView();

  String getTableReference();

  default int getVariableEntityBatchSize() {
    return ENTITY_BATCH_SIZE;
  }

  class Reference {

    private Reference() {}

    public static String getReference(String datasource, String table) {
      return datasource + "." + table;
    }

  }

}

/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import org.obiba.magma.*;

import javax.validation.constraints.NotNull;
import java.util.List;

public abstract class AbstractValueTableWrapper implements ValueTableWrapper {

  @Override
  public abstract ValueTable getWrappedValueTable();

  @NotNull
  @Override
  public Datasource getDatasource() {
    return getWrappedValueTable().getDatasource();
  }

  @Override
  public String getEntityType() {
    return getWrappedValueTable().getEntityType();
  }

  @NotNull
  @Override
  public String getName() {
    return getWrappedValueTable().getName();
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    return getWrappedValueTable().getValue(variable, valueSet);
  }

  @Override
  public List<VariableEntity> getVariableEntities() {
    return getWrappedValueTable().getVariableEntities();
  }

  @Override
  public List<VariableEntity> getVariableEntities(int offset, int limit) {
    return getWrappedValueTable().getVariableEntities(offset, limit);
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return getWrappedValueTable().getValueSet(entity);
  }

  @Override
  public boolean canDropValueSets() {
    return getWrappedValueTable().canDropValueSets();
  }

  @Override
  public void dropValueSets() {
    getWrappedValueTable().dropValueSets();
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getWrappedValueTable().getValueSetTimestamps(entity);
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(Iterable<VariableEntity> entities) {
    return getWrappedValueTable().getValueSetTimestamps(entities);
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return getValueSets(getVariableEntities());
  }

  @Override
  public Iterable<ValueSet> getValueSets(Iterable<VariableEntity> entities) {
    return getWrappedValueTable().getValueSets(entities);
  }

  @Override
  public boolean hasVariable(String name) {
    return getWrappedValueTable().hasVariable(name);
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    return getWrappedValueTable().getVariable(name);
  }

  @Override
  public Iterable<Variable> getVariables() {
    return getWrappedValueTable().getVariables();
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    return getWrappedValueTable().getVariableValueSource(variableName);
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return getWrappedValueTable().hasValueSet(entity);
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getWrappedValueTable().isForEntityType(entityType);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return getWrappedValueTable().getTimestamps();
  }

  @Override
  public boolean isView() {
    return getWrappedValueTable().isView();
  }

  @Override
  public ValueTable getInnermostWrappedValueTable() {
    return getWrappedValueTable() instanceof ValueTableWrapper //
        ? ((ValueTableWrapper) getWrappedValueTable()).getInnermostWrappedValueTable() //
        : getWrappedValueTable();
  }

  @Override
  public String getTableReference() {
    return getWrappedValueTable().getTableReference();
  }

  @Override
  public int getVariableCount() {
    return getWrappedValueTable().getVariableCount();
  }

  @Override
  public int getValueSetCount() {
    return getWrappedValueTable().getValueSetCount();
  }

  @Override
  public int getVariableEntityCount() {
    return getWrappedValueTable().getVariableEntityCount();
  }

  @Override
  public int getVariableEntityBatchSize() {
    return getWrappedValueTable().getVariableEntityBatchSize();
  }

  @Override
  public ValueTableStatus getStatus() {
    return getWrappedValueTable().getStatus();
  }
}

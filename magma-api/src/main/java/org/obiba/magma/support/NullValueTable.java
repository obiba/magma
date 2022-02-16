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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.obiba.magma.*;

import javax.validation.constraints.NotNull;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class NullValueTable implements ValueTable {

  @SuppressWarnings("StaticNonFinalField")
  private static WeakReference<NullValueTable> instance = MagmaEngine.get().registerInstance(new NullValueTable());

  private final List<VariableEntity> entities = ImmutableList.<VariableEntity>builder().build();

  private NullValueTable() {
  }

  @NotNull
  @Override
  @SuppressWarnings("ConstantConditions")
  public String getName() {
    return null;
  }

  @NotNull
  @Override
  @SuppressWarnings("ConstantConditions")
  public Datasource getDatasource() {
    return null;
  }

  @Override
  public String getEntityType() {
    return "";
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return false;
  }

  @Override
  public List<VariableEntity> getVariableEntities() {
    return entities;
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return false;
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return ImmutableList.of();
  }

  @Override
  public Iterable<ValueSet> getValueSets(Iterable<VariableEntity> entities) {
    return ImmutableList.of();
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    throw new NoSuchValueSetException(this, entity);
  }

  @Override
  public boolean canDropValueSets() {
    return true;
  }

  @Override
  public void dropValueSets() {

  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    throw new NoSuchValueSetException(this, entity);
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(Iterable<VariableEntity> entities) {
    List<Timestamps> timestamps = Lists.newArrayList();
    for (VariableEntity entity : entities) {
      timestamps.add(getValueSetTimestamps(entity));
    }
    return timestamps;
  }

  @Override
  public boolean hasVariable(String name) {
    return false;
  }

  @Override
  public Iterable<Variable> getVariables() {
    return Collections.emptyList();
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    throw new NoSuchVariableException("null", name);
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    return variable.getValueType().nullValue();
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    throw new NoSuchVariableException("null", variableName);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }

  @Override
  public boolean isView() {
    return false;
  }

  @Override
  public String getTableReference() {
    return null;
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

  @SuppressWarnings("ConstantConditions")
  @NotNull
  public static NullValueTable get() {
    if (instance == null || instance.get() == null) {
      instance = MagmaEngine.get().registerInstance(new NullValueTable());
    }
    return instance.get();
  }

}
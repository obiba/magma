/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.generated;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.obiba.magma.*;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class GeneratedValueTable implements ValueTable {

  private final Datasource datasource;

  private final Set<Variable> dictionary;

  private final List<VariableEntity> entities;

  private final RandomGenerator randomGenerator;

  private final Map<String, VariableValueSource> generators;

  @NotNull
  private final Value timestamp;

  public GeneratedValueTable(@Nullable Datasource ds, Collection<Variable> dictionary, int entities) {
    this(ds, dictionary, entities, System.currentTimeMillis());
  }

  public GeneratedValueTable(@Nullable Datasource ds, Collection<Variable> dictionary, int entities, long seed) {
    datasource = ds;
    this.dictionary = ImmutableSet.copyOf(dictionary);
    this.entities = Lists.newArrayList();
    randomGenerator = new JDKRandomGenerator();
    randomGenerator.setSeed(seed);
    timestamp = DateTimeType.get().now();
    while (this.entities.size() < entities) {
      VariableEntity entity = generateEntity();
      if (!this.entities.contains(entity)) this.entities.add(entity);
    }

    VariableValueGeneratorFactory factory = new DefaultVariableValueGeneratorFactory();
    generators = Maps.newHashMap();
    for (Variable v : this.dictionary) {
      generators.put(v.getName(), factory.newGenerator(v));
    }
  }

  @NotNull
  @Override
  public Datasource getDatasource() {
    return datasource;
  }

  @Override
  public String getEntityType() {
    return dictionary.iterator().next().getEntityType();
  }

  @NotNull
  @Override
  public String getName() {
    return "generated";
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      @NotNull
      @Override
      public Value getLastUpdate() {
        return timestamp;
      }

      @NotNull
      @Override
      public Value getCreated() {
        return timestamp;
      }

    };
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    return getVariableValueSource(variable.getName()).getValue(valueSet);
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if (entities.contains(entity)) {
      return new GeneratedValueSet(this, entity);
    }
    throw new NoSuchValueSetException(this, entity);
  }

  @Override
  public boolean canDropValueSets() {
    return false;
  }

  @Override
  public void dropValueSets() {

  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getValueSet(entity).getTimestamps();
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(List<VariableEntity> entities) {
    return entities.stream().map(this::getValueSetTimestamps).collect(Collectors.toList());
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return getValueSets(entities);
  }

  @Override
  public Iterable<ValueSet> getValueSets(Iterable<VariableEntity> entities) {
    List<ValueSet> valueSets = Lists.newArrayList();
    for (VariableEntity entity : entities) {
      valueSets.add(new GeneratedValueSet(GeneratedValueTable.this, entity));
    }
    return valueSets;
  }

  @Override
  public Variable getVariable(final String name) throws NoSuchVariableException {
    try {
      return Iterables.find(getVariables(), new Predicate<Variable>() {
        @Override
        public boolean apply(Variable input) {
          return input.getName().equals(name);
        }
      });
    } catch (NoSuchElementException e) {
      throw new NoSuchVariableException(getName(), name);
    }
  }

  @Override
  public List<VariableEntity> getVariableEntities() {
    return Collections.unmodifiableList(entities);
  }

  @Override
  public int getVariableEntityCount() {
    return Iterables.size(getVariableEntities());
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    return generators.get(variableName);
  }

  @Override
  public Iterable<Variable> getVariables() {
    return dictionary;
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return entities.contains(entity);
  }

  @Override
  public boolean hasVariable(String name) {
    return generators.containsKey(name);
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getEntityType().equalsIgnoreCase(entityType);
  }

  private VariableEntity generateEntity() {
    return generateEntity(randomGenerator.nextInt(99999999), 8);
  }

  private VariableEntity generateEntity(long seed, int length) {
    StringBuilder id = new StringBuilder(Long.toString(Math.abs(seed)));
    while (id.length() < length) {
      id.append(0).append(id);
    }
    return new VariableEntityBean(getEntityType(), id.toString());
  }

  @Override
  public boolean isView() {
    return false;
  }

  @Override
  public String getTableReference() {
    return (datasource == null ? "" : datasource.getName()) + "." + getName();
  }

  @Override
  public int getVariableCount() {
    return Iterables.size(getVariables());
  }

  @Override
  public int getValueSetCount() {
    return Iterables.size(getValueSets());
  }

}

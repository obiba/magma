package org.obiba.magma.datasource.generated;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class GeneratedValueTable implements ValueTable {

  private final Datasource datasource;

  private final Set<Variable> dictionary;

  private final Set<VariableEntity> entities;

  private final RandomGenerator randomGenerator;

  private final Map<String, VariableValueSource> generators;

  @Nonnull
  private final Value timestamp;

  public GeneratedValueTable(@Nullable Datasource ds, Collection<Variable> dictionary, int entities) {
    this(ds, dictionary, entities, System.currentTimeMillis());
  }

  public GeneratedValueTable(@Nullable Datasource ds, Collection<Variable> dictionary, int entities, long seed) {
    datasource = ds;
    this.dictionary = ImmutableSet.copyOf(dictionary);
    this.entities = Sets.newTreeSet();
    randomGenerator = new JDKRandomGenerator();
    randomGenerator.setSeed(seed);
    timestamp = DateTimeType.get().now();
    while(this.entities.size() < entities) {
      this.entities.add(generateEntity());
    }

    VariableValueGeneratorFactory factory = new DefaultVariableValueGeneratorFactory();
    generators = Maps.newHashMap();
    for(Variable v : this.dictionary) {
      generators.put(v.getName(), factory.newGenerator(v));
    }
  }

  @Nonnull
  @Override
  public Datasource getDatasource() {
    return datasource;
  }

  @Override
  public String getEntityType() {
    return dictionary.iterator().next().getEntityType();
  }

  @Nonnull
  @Override
  public String getName() {
    return "generated";
  }

  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      @Nonnull
      @Override
      public Value getLastUpdate() {
        return timestamp;
      }

      @Nonnull
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
    if(entities.contains(entity)) {
      return new GeneratedValueSet(this, entity);
    }
    throw new NoSuchValueSetException(this, entity);
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getValueSet(entity).getTimestamps();
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return Iterables.transform(entities, new Function<VariableEntity, ValueSet>() {

      @Override
      public ValueSet apply(VariableEntity from) {
        return new GeneratedValueSet(GeneratedValueTable.this, from);
      }
    });

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
    } catch(NoSuchElementException e) {
      throw new NoSuchVariableException(getName(), name);
    }
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return Collections.unmodifiableSet(entities);
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
    return generateEntity(randomGenerator.nextLong());
  }

  private VariableEntity generateEntity(long seed) {
    return new VariableEntityBean("", Long.toString(Math.abs(seed)));
  }

  @Override
  public boolean isView() {
    return false;
  }

  @Override
  public String getTableReference() {
    return (datasource == null ? "" : datasource.getName()) + "." + getName();
  }

}

package org.obiba.magma.support;

import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public abstract class AbstractValueTable implements ValueTable, Initialisable {

  private Datasource datasource;

  private String name;

  private VariableEntityProvider variableEntityProvider;

  private Set<VariableValueSource> sources = Sets.newLinkedHashSet();

  public AbstractValueTable(Datasource datasource, String name, VariableEntityProvider variableEntityProvider) {
    this.datasource = datasource;
    this.name = name;
    this.variableEntityProvider = variableEntityProvider;
  }

  public AbstractValueTable(Datasource datasource, String name) {
    this(datasource, name, null);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getEntityType() {
    return variableEntityProvider.getEntityType();
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getEntityType().equals(entityType);
  }

  @Override
  public Datasource getDatasource() {
    return datasource;
  }

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return variableEntityProvider.getVariableEntities().contains(entity);
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return Iterables.transform(variableEntityProvider.getVariableEntities(), new Function<VariableEntity, ValueSet>() {
      @Override
      public ValueSet apply(VariableEntity from) {
        return getValueSet(from);
      }
    });
  }

  @Override
  public Variable getVariable(String name) {
    return getVariableValueSource(name).getVariable();
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    return getVariableValueSource(variable.getName()).getValue(valueSet);
  }

  @Override
  public Set<Variable> getVariables() {
    return ImmutableSet.copyOf(Iterables.transform(getSources(), new Function<VariableValueSource, Variable>() {
      @Override
      public Variable apply(VariableValueSource from) {
        return from.getVariable();

      }
    }));
  }

  public VariableValueSource getVariableValueSource(final String name) throws NoSuchVariableException {
    try {
      return Iterables.find(getSources(), new Predicate<VariableValueSource>() {
        @Override
        public boolean apply(VariableValueSource input) {
          return input.getVariable().getName().equals(name);
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchVariableException(getName(), name);
    }
  }

  @Override
  public void initialise() {
    for(Initialisable init : Iterables.filter(getSources(), Initialisable.class)) {
      init.initialise();
    }
  }

  public void setVariableEntityProvider(VariableEntityProvider variableEntityProvider) {
    this.variableEntityProvider = variableEntityProvider;
  }

  protected Set<VariableValueSource> getSources() {
    return sources;
  }

  public void addVariableValueSources(VariableValueSourceFactory factory) {
    sources.addAll(factory.createSources());
  }

  public void addVariableValueSources(Set<VariableValueSource> sources) {
    this.sources.addAll(sources);
  }

  public void addVariableValueSource(VariableValueSource source) {
    sources.add(source);
  }
}

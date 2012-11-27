package org.obiba.magma.support;

import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

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

  private final Datasource datasource;

  private final String name;

  private final Set<VariableValueSource> sources = Sets.newLinkedHashSet();

  private VariableEntityProvider variableEntityProvider;

  public AbstractValueTable(@Nullable Datasource datasource, @Nullable String name,
      @Nullable VariableEntityProvider variableEntityProvider) {
    if(datasource == null) throw new IllegalArgumentException("datasource cannot be null");
    if(name == null) throw new IllegalArgumentException("name cannot be null");
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
    return getVariableEntityProvider().getEntityType();
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
    return getVariableEntityProvider().getVariableEntities().contains(entity);
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return Collections.unmodifiableSet(variableEntityProvider.getVariableEntities());
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return Iterables
        .transform(getVariableEntityProvider().getVariableEntities(), new Function<VariableEntity, ValueSet>() {
          @Override
          public ValueSet apply(VariableEntity from) {
            return getValueSet(from);
          }
        });
  }

  @Override
  public boolean hasVariable(String variableName) {
    for(VariableValueSource source : getSources()) {
      if(source.getVariable().getName().equals(variableName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Variable getVariable(String variableName) {
    return getVariableValueSource(variableName).getVariable();
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

  @Override
  public VariableValueSource getVariableValueSource(final String variableName) throws NoSuchVariableException {
    try {
      return Iterables.find(getSources(), new Predicate<VariableValueSource>() {
        @Override
        public boolean apply(VariableValueSource input) {
          return input.getVariable().getName().equals(variableName);
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchVariableException(getName(), variableName);
    }
  }

  @Override
  public void initialise() {
    Initialisables.initialise(getSources());
  }

  protected void addVariableValueSources(VariableValueSourceFactory factory) {
    sources.addAll(factory.createSources());
  }

  protected void addVariableValueSources(
      @SuppressWarnings("ParameterHidesMemberVariable") Collection<VariableValueSource> sources) {
    this.sources.removeAll(sources);
    this.sources.addAll(sources);
  }

  protected void addVariableValueSource(VariableValueSource source) {
    sources.add(source);
  }

  protected void setVariableEntityProvider(VariableEntityProvider variableEntityProvider) {
    if(variableEntityProvider == null) throw new IllegalArgumentException("variableEntityProvider cannot be null");
    this.variableEntityProvider = variableEntityProvider;
  }

  protected VariableEntityProvider getVariableEntityProvider() {
    if(variableEntityProvider == null) throw new IllegalArgumentException("variableEntityProvider cannot be null");
    return variableEntityProvider;
  }

  protected Set<VariableValueSource> getSources() {
    return sources;
  }

  @Override
  public boolean isView() {
    return false;
  }

}

package org.obiba.magma.support;

import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public abstract class AbstractValueTable implements ValueTable, Initialisable {

  @NotNull
  private final Datasource datasource;

  @NotNull
  protected String name;

  private final Set<VariableValueSource> sources = Sets.newLinkedHashSet();

  private VariableEntityProvider variableEntityProvider;

  @SuppressWarnings("ConstantConditions")
  public AbstractValueTable(@NotNull Datasource datasource, @NotNull String name,
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
  @NotNull
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
  @NotNull
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
    // Filter null
    Iterable<Variable> transform = Iterables.transform(getSources(), new Function<VariableValueSource, Variable>() {
      @Override
      public Variable apply(VariableValueSource from) {
        return from.getVariable();
      }
    });

    return ImmutableSet.copyOf(Iterables.filter(transform, Predicates.not(Predicates.isNull())));
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

  protected void addVariableValueSources(Collection<VariableValueSource> sourcesToAdd) {
    removeVariableValueSources(sourcesToAdd);
    sources.addAll(sourcesToAdd);
  }

  protected void addVariableValueSource(VariableValueSource source) {
    sources.add(source);
  }

  protected void removeVariableValueSource(String variableName) {
    try {
      VariableValueSource vvs = getVariableValueSource(variableName);
      getSources().remove(vvs);
    } catch(NoSuchVariableException ex) {
      // ignore
    }
  }

  protected void removeVariableValueSources(Collection<VariableValueSource> sourcesToAdd) {
    sources.removeAll(sourcesToAdd);
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

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getValueSet(entity).getTimestamps();
  }

  @Override
  public String getTableReference() {
    return getDatasource().getName() + "." + getName();
  }

  @Override
  public int hashCode() {return Objects.hash(datasource, name);}

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }
    AbstractValueTable other = (AbstractValueTable) obj;
    return Objects.equals(datasource, other.datasource) && Objects.equals(name, other.name);
  }
}

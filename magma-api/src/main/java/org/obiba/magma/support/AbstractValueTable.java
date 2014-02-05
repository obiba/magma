package org.obiba.magma.support;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public abstract class AbstractValueTable implements ValueTable, Initialisable {

  @NotNull
  private final Datasource datasource;

  @NotNull
  protected String name;

  private final Map<String, VariableValueSource> sources = new LinkedHashMap<>();

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
    return Objects.equals(getEntityType(), entityType);
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
    return sources.containsKey(variableName);
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
    Iterable<Variable> variables = Iterables.transform(getSources(), new Function<VariableValueSource, Variable>() {
      @Override
      public Variable apply(VariableValueSource from) {
        return from.getVariable();
      }
    });
    // Filter null
    return ImmutableSet.copyOf(Iterables.filter(variables, Predicates.not(Predicates.isNull())));
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    VariableValueSource variableValueSource = sources.get(variableName);
    if(variableValueSource == null) {
      throw new NoSuchVariableException(getName(), variableName);
    }
    return variableValueSource;
  }

  @Override
  public void initialise() {
    Initialisables.initialise(getSources());
  }

  protected void addVariableValueSources(VariableValueSourceFactory factory) {
    for(VariableValueSource variableValueSource : factory.createSources()) {
      sources.put(variableValueSource.getName(), variableValueSource);
    }
  }

  protected void addVariableValueSources(Collection<VariableValueSource> sourcesToAdd) {
    List<VariableValueSource> list = Lists.newArrayList(sources.values());
    for(VariableValueSource variableValueSource : sourcesToAdd) {
      int index = list.indexOf(variableValueSource);
      if(index >= 0) {
        list.remove(index);
        list.add(index, variableValueSource);
      } else {
        list.add(variableValueSource);
      }
    }
    sources.clear();
    for(VariableValueSource variableValueSource : list) {
      sources.put(variableValueSource.getName(), variableValueSource);
    }
  }

  protected void addVariableValueSource(VariableValueSource source) {
    sources.put(source.getName(), source);
  }

  protected void removeVariableValueSource(String variableName) {
    sources.remove(variableName);
  }

  protected void removeVariableValueSources(Iterable<VariableValueSource> sourcesToRemove) {
    for(VariableValueSource variableValueSource : sourcesToRemove) {
      removeVariableValueSource(variableValueSource.getVariable().getName());
    }
  }

  protected void setVariableEntityProvider(@NotNull VariableEntityProvider variableEntityProvider) {
    //noinspection ConstantConditions
    if(variableEntityProvider == null) throw new IllegalArgumentException("variableEntityProvider cannot be null");
    this.variableEntityProvider = variableEntityProvider;
  }

  @NotNull
  protected VariableEntityProvider getVariableEntityProvider() {
    if(variableEntityProvider == null) throw new IllegalArgumentException("variableEntityProvider cannot be null");
    return variableEntityProvider;
  }

  protected Set<VariableValueSource> getSources() {
    return ImmutableSet.copyOf(sources.values());
  }

  protected void clearSources() {
    sources.clear();
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
    return ValueTable.Reference.getReference(getDatasource().getName(), getName());
  }

  @Override
  public int getVariableCount() {
    return getVariables().size();
  }

  @Override
  public int getValueSetCount() {
    return Iterables.size(getValueSets());
  }

  @Override
  public int getVariableEntityCount() {
    return getVariableEntities().size();
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

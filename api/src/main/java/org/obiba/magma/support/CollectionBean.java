package org.obiba.magma.support;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.Collection;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSetProvider;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class CollectionBean implements Collection, Initialisable {

  private Datasource datasource;

  private String name;

  private Set<ValueSetProvider> valueSetProviders = new HashSet<ValueSetProvider>();

  private Set<VariableValueSource> variableSources = new HashSet<VariableValueSource>();

  CollectionBean(Datasource datasource, String name) {
    this.datasource = datasource;
    this.name = name;
  }

  void setValueSetProviders(Set<ValueSetProvider> valueSetProviders) {
    this.valueSetProviders = valueSetProviders;
  }

  void setVariableValueSources(Set<VariableValueSource> variableSources) {
    this.variableSources = variableSources;
  }

  @Override
  public Set<String> getEntityTypes() {
    return ImmutableSet.copyOf(Iterables.transform(valueSetProviders, new Function<ValueSetProvider, String>() {
      @Override
      public String apply(ValueSetProvider from) {
        return from.getEntityType();
      }
    }));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Datasource getDatasource() {
    return datasource;
  }

  @Override
  public Set<VariableEntity> getEntities(String entityType) {
    return lookupProvider(entityType).getVariableEntities();
  }

  @Override
  public ValueSet loadValueSet(VariableEntity entity) {
    return new ValueSetBean(this, entity);
  }

  @Override
  public VariableValueSource getVariableValueSource(final String entityType, final String variableName) throws NoSuchVariableException {
    try {
      return Iterables.find(variableSources, new Predicate<VariableValueSource>() {
        @Override
        public boolean apply(VariableValueSource input) {
          return input.getVariable().getName().equals(variableName) && input.getVariable().isForEntityType(entityType);
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchVariableException(getName(), variableName);
    }
  }

  @Override
  public Set<VariableValueSource> getVariableValueSources(final String entityType) {
    return ImmutableSet.copyOf(Iterables.filter(variableSources, new Predicate<VariableValueSource>() {
      @Override
      public boolean apply(VariableValueSource input) {
        return input.getVariable().isForEntityType(entityType);
      }
    }));
  }

  @Override
  public Set<Variable> getVariables(String entityType) {
    return ImmutableSet.copyOf(Iterables.transform(getVariableValueSources(entityType), new Function<VariableValueSource, Variable>() {
      @Override
      public Variable apply(VariableValueSource from) {
        return from.getVariable();
      }
    }));

  }

  @Override
  public void initialise() {
    for(Initialisable init : Iterables.filter(Iterables.concat(variableSources, valueSetProviders), Initialisable.class)) {
      init.initialise();
    }
  }

  protected ValueSetProvider lookupProvider(final String entityType) {
    try {
      return Iterables.find(this.valueSetProviders, new Predicate<ValueSetProvider>() {
        @Override
        public boolean apply(ValueSetProvider input) {
          return input.isForEntityType(entityType);
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchValueSetException(null);
    }
  }
}

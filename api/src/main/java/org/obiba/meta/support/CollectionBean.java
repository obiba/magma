package org.obiba.meta.support;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.Initialisable;
import org.obiba.meta.NoSuchValueSetException;
import org.obiba.meta.NoSuchVariableException;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueSetReferenceProvider;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class CollectionBean implements Collection, Initialisable {

  private String name;

  private Set<ValueSetReferenceProvider> valueSetProviders = new HashSet<ValueSetReferenceProvider>();

  private Set<VariableValueSource> variableSources = new HashSet<VariableValueSource>();

  CollectionBean(String name) {
    this.name = name;
  }

  void setValueSetProviders(Set<ValueSetReferenceProvider> valueSetProviders) {
    this.valueSetProviders = valueSetProviders;
  }

  void setVariableValueSources(Set<VariableValueSource> variableSources) {
    this.variableSources = variableSources;
  }

  @Override
  public Set<String> getEntityTypes() {
    return ImmutableSet.copyOf(Iterables.transform(valueSetProviders, new Function<ValueSetReferenceProvider, String>() {
      @Override
      public String apply(ValueSetReferenceProvider from) {
        return from.getEntityType();
      }
    }));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Set<ValueSetReference> getValueSetReferences(String entityType) {
    return lookupProvider(entityType).getValueSetReferences();
  }

  @Override
  public VariableValueSource getVariableValueSource(final String entityType, final String variableName) {
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
  public Set<Variable> getVariables() {
    return ImmutableSet.copyOf(Iterables.transform(variableSources, new Function<VariableValueSource, Variable>() {
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

  protected ValueSetReferenceProvider lookupProvider(final String entityType) {
    try {
      return Iterables.find(this.valueSetProviders, new Predicate<ValueSetReferenceProvider>() {
        @Override
        public boolean apply(ValueSetReferenceProvider input) {
          return input.isForEntityType(entityType);
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchValueSetException(null);
    }
  }
}

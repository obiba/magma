package org.obiba.meta.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.Initialisable;
import org.obiba.meta.NoSuchValueSetException;
import org.obiba.meta.NoSuchVariableException;
import org.obiba.meta.ValueSet;
import org.obiba.meta.ValueSetExtension;
import org.obiba.meta.ValueSetProvider;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableEntity;
import org.obiba.meta.VariableValueSource;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class CollectionBean implements Collection, Initialisable {

  private String name;

  private Set<ValueSetProvider> valueSetProviders = new HashSet<ValueSetProvider>();

  private Set<VariableValueSource> variableSources = new HashSet<VariableValueSource>();

  private Map<String, ValueSetExtension> extensions = new HashMap<String, ValueSetExtension>();

  CollectionBean(String name) {
    this.name = name;
  }

  void setValueSetProviders(Set<ValueSetProvider> valueSetProviders) {
    this.valueSetProviders = valueSetProviders;
  }

  void setVariableValueSources(Set<VariableValueSource> variableSources) {
    this.variableSources = variableSources;
  }

  void addExtension(String name, ValueSetExtension extension) {
    extensions.put(name, extension);
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
  public Set<VariableEntity> getEntities(String entityType) {
    return lookupProvider(entityType).getVariableEntities();
  }

  @Override
  public ValueSet loadValueSet(VariableEntity entity) {
    return lookupProvider(entity.getType()).loadValueSet(this, entity);
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

  @Override
  public ValueSetExtension<?, ?> getExtension(String name) {
    ValueSetExtension<?, ?> extension = extensions.get(name);
    if(extension == null) {
      throw new IllegalArgumentException("Cannot extend ValueSet for '" + name + "'");
    }
    return extension;
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

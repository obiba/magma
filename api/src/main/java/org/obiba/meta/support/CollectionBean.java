package org.obiba.meta.support;

import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.IValueSetReference;
import org.obiba.meta.IValueSetReferenceProvider;
import org.obiba.meta.IVariableValueSource;
import org.obiba.meta.IVariableValueSourceProvider;
import org.obiba.meta.NoSuchVariableException;

import com.google.common.collect.ImmutableSet;

public class CollectionBean implements Collection {

  private String name;

  private Set<IValueSetReferenceProvider> valueSets;

  private Set<IVariableValueSourceProvider> variableValueSourceProviders;

  private Set<IVariableValueSource> variableValueSources;

  @Override
  public Set<String> getEntityTypes() {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for(IValueSetReferenceProvider valueSet : valueSets) {
      builder.add(valueSet.getEntityType());
    }
    return builder.build();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Set<IValueSetReference> getValueSetReferences(String entityType) {
    return getValueSetProvider(entityType).getValueSetReferences();
  }

  @Override
  public IValueSetReferenceProvider getValueSetProvider(String entityType) {
    for(IValueSetReferenceProvider valueSet : valueSets) {
      if(valueSet.getEntityType().equals(entityType)) {
        return valueSet;
      }
    }
    // No such provider
    throw new IllegalArgumentException(entityType);
  }

  @Override
  public Set<IVariableValueSource> getVariables(String entityType) {
    ImmutableSet.Builder<IVariableValueSource> b = ImmutableSet.builder();
    b.addAll(variableValueSources);
    for(IVariableValueSourceProvider provider : variableValueSourceProviders) {
      b.addAll(provider.getVariables());
    }
    return b.build();
  }

  @Override
  public IVariableValueSource getVariable(String entityType, String variableName) {
    for(IVariableValueSource source : getVariables(entityType)) {
      if(source.getVariable().getName().equals(name)) {
        return source;
      }
    }
    throw new NoSuchVariableException(getName(), name);
  }
}

package org.obiba.meta.support;

import java.util.Set;

import org.obiba.meta.CollectionConnector;
import org.obiba.meta.ValueSetReference;
import org.obiba.meta.ValueSetReferenceProvider;
import org.obiba.meta.Variable;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.VariableValueSourceFactory;

import com.google.common.collect.ImmutableSet;

public class ConnectorBean implements CollectionConnector {

  private ValueSetReferenceProvider provider;

  private Set<VariableValueSourceFactory> factories;

  private Set<VariableValueSource> sources;

  private Set<VariableValueSource> mergedSources;

  @Override
  public String getEntityType() {
    return provider.getEntityType();
  }

  @Override
  public VariableValueSource getVariableValueSource(String name) {
    VariableValueSource source = findNamedSource(name);
    if(source == null) {
      throw new IllegalArgumentException();
    }
    return source;
  }

  @Override
  public Set<VariableValueSource> getVariableValueSources() {
    return mergedSources;
  }

  @Override
  public Set<Variable> getVariables() {
    return null;
  }

  @Override
  public Set<ValueSetReference> getValueSetReferences() {
    return provider.getValueSetReferences();
  }

  @Override
  public boolean hasVariableValueSource(String name) {
    return findNamedSource(name) != null;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return provider.isForEntityType(entityType);
  }

  @Override
  public boolean contains(ValueSetReference reference) {
    return provider.contains(reference);
  }

  protected VariableValueSource findNamedSource(String name) {
    for(VariableValueSource source : sources) {
      if(source.getVariable().getName().equals(name)) {
        return source;
      }
    }
    return null;
  }

  protected void doMergeSources() {
    ImmutableSet.Builder<VariableValueSource> builder = new ImmutableSet.Builder<VariableValueSource>();
    builder.addAll(sources);
    for(VariableValueSourceFactory factory : factories) {
      builder.addAll(factory.createSources());
    }
    mergedSources = builder.build();
  }
}

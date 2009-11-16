package org.obiba.magma.support;

import org.obiba.magma.Collection;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSetProvider;
import org.obiba.magma.VariableValueSource;

import com.google.common.collect.ImmutableSet;

public class CollectionBuilder {

  private String name;

  private ImmutableSet.Builder<ValueSetProvider> providerBuilder = new ImmutableSet.Builder<ValueSetProvider>();

  private ImmutableSet.Builder<VariableValueSource> sourceBuilder = new ImmutableSet.Builder<VariableValueSource>();

  public CollectionBuilder(String name) {
    this.name = name;
  }

  public CollectionBuilder add(ValueSetProvider provider) {
    providerBuilder.add(provider);
    return this;
  }

  public CollectionBuilder add(VariableValueSource... sources) {
    sourceBuilder.add(sources);
    return this;
  }

  public CollectionBuilder add(Iterable<VariableValueSource> sources) {
    sourceBuilder.addAll(sources);
    return this;
  }

  public Collection build(Datasource datasource) {
    CollectionBean collection = new CollectionBean(datasource, name);
    collection.setValueSetProviders(providerBuilder.build());
    collection.setVariableValueSources(sourceBuilder.build());
    return collection;
  }

}

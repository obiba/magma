package org.obiba.meta.support;

import org.obiba.meta.Collection;
import org.obiba.meta.ValueSetExtension;
import org.obiba.meta.ValueSetProvider;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.VariableValueSourceFactory;

import com.google.common.collect.ImmutableSet;

public class CollectionBuilder {

  private CollectionBean collection;

  private ImmutableSet.Builder<ValueSetProvider> providerBuilder = new ImmutableSet.Builder<ValueSetProvider>();

  private ImmutableSet.Builder<VariableValueSource> sourceBuilder = new ImmutableSet.Builder<VariableValueSource>();

  public CollectionBuilder(String name) {
    collection = new CollectionBean(name);
  }

  public CollectionBuilder add(ValueSetProvider provider) {
    providerBuilder.add(provider);
    return this;
  }

  public CollectionBuilder add(VariableValueSource... sources) {
    sourceBuilder.add(sources);
    return this;
  }

  public CollectionBuilder add(VariableValueSourceFactory factory) {
    sourceBuilder.addAll(factory.createSources(collection.getName()));
    return this;
  }

  public CollectionBuilder add(String name, ValueSetExtension extension) {
    collection.addExtension(name, extension);
    return this;
  }

  public Collection build() {
    collection.setValueSetProviders(providerBuilder.build());
    collection.setVariableValueSources(sourceBuilder.build());
    return collection;
  }

}

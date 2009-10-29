package org.obiba.meta.support;

import org.obiba.meta.Collection;
import org.obiba.meta.ValueSetReferenceProvider;
import org.obiba.meta.VariableValueSource;
import org.obiba.meta.VariableValueSourceFactory;

public class CollectionBuilder {

  private CollectionBean collection;

  public CollectionBuilder(String name) {
    collection = new CollectionBean(name);
  }

  public CollectionBuilder add(ValueSetReferenceProvider provider) {
    collection.addValueSetReferenceProvider(provider);
    return this;
  }

  public CollectionBuilder add(VariableValueSource... sources) {
    collection.addVariableValueSource(sources);
    return this;
  }

  public CollectionBuilder add(VariableValueSourceFactory factory) {
    collection.addVariableValueSource(factory);
    return this;
  }

  public Collection build() {
    collection.initialise();
    return collection;
  }

}

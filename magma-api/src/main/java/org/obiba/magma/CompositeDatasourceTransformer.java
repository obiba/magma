package org.obiba.magma;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("UnusedDeclaration")
public class CompositeDatasourceTransformer implements DatasourceTransformer {

  @Nonnull
  private final List<DatasourceTransformer> transformers;

  // Public default ctor for XStream de-ser
  public CompositeDatasourceTransformer() {
    transformers = ImmutableList.of();
  }

  public CompositeDatasourceTransformer(@Nonnull Collection<DatasourceTransformer> transformers) {
    if(transformers == null) throw new IllegalArgumentException("transformers cannot be null");
    this.transformers = ImmutableList.copyOf(transformers);
  }

  @Override
  public Datasource transform(Datasource datasource) {
    Datasource transformed = datasource;
    for(DatasourceTransformer transformer : transformers) {
      transformed = transformer.transform(transformed);
    }
    return transformed;
  }

}

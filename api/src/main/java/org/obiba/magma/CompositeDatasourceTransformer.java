package org.obiba.magma;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class CompositeDatasourceTransformer implements DatasourceTransformer {

  private List<DatasourceTransformer> transformers;

  // Public default ctor for XStream de-ser
  public CompositeDatasourceTransformer() {
    transformers = ImmutableList.of();
  }

  public CompositeDatasourceTransformer(List<DatasourceTransformer> transformers) {
    if(transformers == null) throw new IllegalArgumentException("transformers cannot be null");
    this.transformers = ImmutableList.copyOf(transformers);
  }

  @Override
  public Datasource transform(Datasource datasource) {
    for(DatasourceTransformer transformer : transformers) {
      datasource = transformer.transform(datasource);
    }
    return datasource;
  }

}

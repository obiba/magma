package org.obiba.magma;

import java.util.List;

public class CompositeDatasourceTransformer implements DatasourceTransformer {
  //
  // Instance Variables
  //

  private List<DatasourceTransformer> transformers;

  //
  // DatasourceTransformer Methods
  //

  @Override
  public Datasource transform(Datasource datasource) {
    if(transformers != null) {
      for(DatasourceTransformer transformer : transformers) {
        datasource = transformer.transform(datasource);
      }
    }

    return datasource;
  }

}

package org.obiba.magma;

public abstract class AbstractDatasourceFactory implements DatasourceFactory {
  //
  // Instance Variables
  //

  protected DatasourceTransformer transformer;

  //
  // DatasourceFactory Methods
  //

  public abstract Datasource create();

  public void setDatasourceTransformer(DatasourceTransformer transformer) {
    this.transformer = transformer;
  }

}

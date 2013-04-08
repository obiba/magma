package org.obiba.magma;

import javax.annotation.Nonnull;

public abstract class AbstractDatasourceFactory implements DatasourceFactory {
  //
  // Instance Variables
  //

  @Deprecated
  private DatasourceTransformer transformer;

  private String name;

  @Nonnull
  protected abstract Datasource internalCreate();

  //
  // DatasourceFactory Methods
  //

  @Override
  public void setName(String name) {
    if(name == null) throw new IllegalArgumentException("Datasource name cannot be null.");
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Datasource create() {
    return internalCreate();
  }

  @Override
  public void setDatasourceTransformer(DatasourceTransformer transformer) {
    this.transformer = transformer;
  }

  @Override
  public DatasourceTransformer getDatasourceTransformer() {
    return transformer;
  }

}

package org.obiba.magma;

import javax.validation.constraints.NotNull;

public abstract class AbstractDatasourceFactory implements DatasourceFactory {
  //
  // Instance Variables
  //

  @Deprecated
  private DatasourceTransformer transformer;

  private String name;

  @NotNull
  protected abstract Datasource internalCreate();

  //
  // DatasourceFactory Methods
  //

  @SuppressWarnings("ConstantConditions")
  @Override
  public void setName(@NotNull String name) {
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

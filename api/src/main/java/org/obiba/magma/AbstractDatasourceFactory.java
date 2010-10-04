package org.obiba.magma;

public abstract class AbstractDatasourceFactory implements DatasourceFactory {
  //
  // Instance Variables
  //

  private DatasourceTransformer transformer;

  private String name;

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
    return this.name;
  }

  @Override
  public Datasource create() {
    Datasource datasource = internalCreate();
    return (transformer != null) ? transformer.transform(datasource) : datasource;
  }

  @Override
  public void setDatasourceTransformer(DatasourceTransformer transformer) {
    this.transformer = transformer;
  }

  @Override
  public DatasourceTransformer getDatasourceTransformer() {
    return this.transformer;
  }

}

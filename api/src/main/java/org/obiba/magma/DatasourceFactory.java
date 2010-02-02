package org.obiba.magma;

public interface DatasourceFactory {

  public Datasource create();

  /**
   * Configures the factory with a "strategy" that adapts the behaviour of the <code>create</code> method.
   * 
   * When a strategy is configured, invoking the <code>create</code> causes a datasource to be created and then modified
   * by the strategy (a {@link DatasourceTransformer}). The resulting datasource is returned.
   * 
   * @param transformer datasource transformer
   */
  public void setDatasourceTransformer(DatasourceTransformer transformer);

}

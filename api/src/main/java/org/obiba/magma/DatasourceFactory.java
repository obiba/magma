package org.obiba.magma;

public interface DatasourceFactory {

  /**
   * Name of the datasource instance that will be created.
   */
  public void setName(String name);

  /**
   * Get the name of the datasource that will be created.
   * @return
   */
  public String getName();

  /**
   * Create the datasource.
   * @return
   */
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

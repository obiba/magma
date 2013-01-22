package org.obiba.magma;

public interface DatasourceFactory {

  /**
   * Name of the datasource instance that will be created.
   */
  void setName(String name);

  /**
   * Get the name of the datasource that will be created.
   *
   * @return
   */
  String getName();

  /**
   * Create the datasource.
   *
   * @return
   */
  Datasource create();

  /**
   * Configures the factory with a "strategy" that adapts the behaviour of the <code>create</code> method.
   * <p/>
   * When a strategy is configured, invoking the <code>create</code> causes a datasource to be created and then modified
   * by the strategy (a {@link DatasourceTransformer}). The resulting datasource is returned.
   *
   * @param transformer datasource transformer
   * @deprecated Transformers are no longer read from the opal-config.xml file as part of a datasource configuration.
   *             Method is kept to allow an old configuration to be read and upgraded automatically with an upgrade script.
   */
  void setDatasourceTransformer(DatasourceTransformer transformer);

  /**
   * @return datasource transformer
   * @deprecated Used only by the upgrade script to retrieve transformers during an upgrade.
   */
  DatasourceTransformer getDatasourceTransformer();

}

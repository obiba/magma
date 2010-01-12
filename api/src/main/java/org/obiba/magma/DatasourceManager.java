package org.obiba.magma;

import java.util.Set;

/**
 * A factory of {@code Datasource} instances that also allows deleting and listing available instances.
 * @param <T> the type of {@code Datasource} managed by this type
 */
public interface DatasourceManager<T extends Datasource> {

  public Set<String> listAvailableDatasources();

  public T open(String datasource);

  public T create(String datasource);

  public void delete(String datasource);

}

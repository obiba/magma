package org.obiba.magma;

public interface DatasourceFactory<T extends Datasource> {

  public T create();

}

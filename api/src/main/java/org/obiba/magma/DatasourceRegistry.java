package org.obiba.magma;

import java.util.Set;

import org.obiba.magma.support.ValueTableReference;

public interface DatasourceRegistry {

  ValueTableReference createReference(String reference);

  Set<Datasource> getDatasources();

  Datasource getDatasource(String name) throws NoSuchDatasourceException;

  boolean hasDatasource(String name);

  void addDecorator(Decorator<Datasource> decorator);

  Datasource addDatasource(Datasource datasource);

  Datasource addDatasource(DatasourceFactory factory);

  void removeDatasource(Datasource datasource);

  String addTransientDatasource(DatasourceFactory factory);

  boolean hasTransientDatasource(String uid);

  void removeTransientDatasource(String uid);

  Datasource getTransientDatasourceInstance(String uid);

}

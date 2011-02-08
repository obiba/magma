package org.obiba.magma;

import java.util.Set;

import org.obiba.magma.support.ValueTableReference;

public interface DatasourceRegistry {

  public ValueTableReference createReference(String reference);

  public Set<Datasource> getDatasources();

  public Datasource getDatasource(final String name) throws NoSuchDatasourceException;

  public boolean hasDatasource(final String name);

  public void addDecorator(Decorator<Datasource> decorator);

  public Datasource addDatasource(Datasource datasource);

  public Datasource addDatasource(final DatasourceFactory factory);

  public void removeDatasource(final Datasource datasource);

  public String addTransientDatasource(final DatasourceFactory factory);

  public boolean hasTransientDatasource(final String uid);

  public void removeTransientDatasource(final String uid);

  public Datasource getTransientDatasourceInstance(final String uid);

}

package org.obiba.magma.datasource.hibernate.support;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.support.Initialisables;

public class HibernateDatasourceFactory extends AbstractDatasourceFactory implements Initialisable {

  private String name;

  private SessionFactoryProvider sessionFactoryProvider;

  public HibernateDatasourceFactory() {

  }

  public HibernateDatasourceFactory(String name, SessionFactoryProvider sessionFactoryProvider) {
    this.name = name;
    this.sessionFactoryProvider = sessionFactoryProvider;
  }

  public Datasource create() {
    HibernateDatasource datasource = new HibernateDatasource(name, sessionFactoryProvider.getSessionFactory());
    return (transformer != null) ? transformer.transform(datasource) : datasource;
  }

  @Override
  public void initialise() {
    Initialisables.initialise(sessionFactoryProvider);
  }

  public String getDatasourceName() {
    return name;
  }

  public SessionFactoryProvider getSessionFactoryProvider() {
    return sessionFactoryProvider;
  }
}

package org.obiba.magma.datasource.hibernate.support;

import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.Initialisable;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.support.Initialisables;

public class HibernateDatasourceFactory implements DatasourceFactory<HibernateDatasource>, Initialisable {

  private String name;

  private SessionFactoryProvider sessionFactoryProvider;

  public HibernateDatasourceFactory() {

  }

  public HibernateDatasourceFactory(String name, SessionFactoryProvider sessionFactoryProvider) {
    this.name = name;
    this.sessionFactoryProvider = sessionFactoryProvider;
  }

  public HibernateDatasource create() {
    return new HibernateDatasource(name, sessionFactoryProvider.getSessionFactory());
  }

  @Override
  public void initialise() {
    Initialisables.initialise(sessionFactoryProvider);
  }

}

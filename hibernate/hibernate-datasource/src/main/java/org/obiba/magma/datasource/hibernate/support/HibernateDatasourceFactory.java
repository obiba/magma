package org.obiba.magma.datasource.hibernate.support;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.support.Initialisables;

public class HibernateDatasourceFactory extends AbstractDatasourceFactory implements Initialisable {

  private SessionFactoryProvider sessionFactoryProvider;

  public HibernateDatasourceFactory() {

  }

  public HibernateDatasourceFactory(String name, SessionFactoryProvider sessionFactoryProvider) {
    setName(name);
    this.sessionFactoryProvider = sessionFactoryProvider;
  }

  public Datasource internalCreate() {
    return new HibernateDatasource(getName(), sessionFactoryProvider.getSessionFactory());
  }

  @Override
  public void initialise() {
    Initialisables.initialise(sessionFactoryProvider);
  }

  public SessionFactoryProvider getSessionFactoryProvider() {
    return sessionFactoryProvider;
  }
}

package org.obiba.magma.datasource.hibernate.support;

import javax.annotation.Nonnull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;

@SuppressWarnings("UnusedDeclaration")
public class HibernateDatasourceFactory extends AbstractDatasourceFactory implements Initialisable, Disposable {

  private SessionFactoryProvider sessionFactoryProvider;

  public HibernateDatasourceFactory() {

  }

  public HibernateDatasourceFactory(String name, SessionFactoryProvider sessionFactoryProvider) {
    setName(name);
    this.sessionFactoryProvider = sessionFactoryProvider;
  }

  @Nonnull
  @Override
  public Datasource internalCreate() {
    return new HibernateDatasource(getName(), sessionFactoryProvider.getSessionFactory());
  }

  public void setSessionFactoryProvider(SessionFactoryProvider sessionFactoryProvider) {
    this.sessionFactoryProvider = sessionFactoryProvider;
  }

  public SessionFactoryProvider getSessionFactoryProvider() {
    return sessionFactoryProvider;
  }

  @Override
  public void initialise() {
    Initialisables.initialise(sessionFactoryProvider);
  }

  @Override
  public void dispose() {
    Disposables.dispose(sessionFactoryProvider);
  }

}

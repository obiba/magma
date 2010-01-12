package org.obiba.magma.datasource.hibernate;

import java.util.List;
import java.util.Set;

import org.hibernate.StatelessSession;
import org.obiba.magma.DatasourceManager;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.datasource.hibernate.domain.DatasourceState;
import org.obiba.magma.support.Initialisables;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class HibernateDatasourceManager implements DatasourceManager<HibernateDatasource>, Initialisable {

  private SessionFactoryProvider sessionFactoryProvider;

  public HibernateDatasourceManager(SessionFactoryProvider provider) {
    if(provider == null) throw new NullPointerException("provider cannot be null");
    this.sessionFactoryProvider = provider;
  }

  @Override
  public void initialise() {
    Initialisables.initialise(sessionFactoryProvider);
  }

  @Override
  public Set<String> listAvailableDatasources() {
    // A stateless session allows criterias outside of a transaction.
    StatelessSession session = sessionFactoryProvider.getSessionFactory().openStatelessSession();
    try {
      List<DatasourceState> datasources = session.createCriteria(DatasourceState.class).list();
      return ImmutableSet.copyOf(Iterables.transform(datasources, new Function<DatasourceState, String>() {
        @Override
        public String apply(DatasourceState from) {
          return from.getName();
        }
      }));
    } finally {
      session.close();
    }
  }

  @Override
  public HibernateDatasource create(String datasource) {
    if(listAvailableDatasources().contains(datasource)) {
      throw new MagmaRuntimeException("Datasource '" + datasource + "' already exists.");
    }
    return new HibernateDatasource(datasource, sessionFactoryProvider.getSessionFactory());
  }

  @Override
  public void delete(String datasource) {
    // TODO: implement me
    throw new UnsupportedOperationException();
  }

  @Override
  public HibernateDatasource open(String datasource) {
    if(listAvailableDatasources().contains(datasource) == false) {
      throw new MagmaRuntimeException("Datasource '" + datasource + "' does not exist.");
    }
    return new HibernateDatasource(datasource, sessionFactoryProvider.getSessionFactory());
  }
}

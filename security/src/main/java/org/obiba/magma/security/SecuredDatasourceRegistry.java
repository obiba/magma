package org.obiba.magma.security;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.DatasourceRegistry;
import org.obiba.magma.Decorator;
import org.obiba.magma.NoSuchDatasourceException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class SecuredDatasourceRegistry implements DatasourceRegistry {

  private final Authorizer authorizer;

  private final DatasourceRegistry delegate;

  public SecuredDatasourceRegistry(Authorizer authorizer, DatasourceRegistry datasourceRegistry) {
    this.authorizer = authorizer;
    this.delegate = datasourceRegistry;
    this.addDecorator(new SecuredDatasourceDecorator());
  }

  public Datasource addDatasource(Datasource datasource) {
    return delegate.addDatasource(datasource);
  }

  public Datasource addDatasource(DatasourceFactory factory) {
    return delegate.addDatasource(factory);
  }

  public void addDecorator(Decorator<Datasource> decorator) {
    delegate.addDecorator(decorator);
  }

  public String addTransientDatasource(DatasourceFactory factory) {
    return delegate.addTransientDatasource(factory);
  }

  public Datasource getDatasource(String name) throws NoSuchDatasourceException {
    Datasource ds = delegate.getDatasource(name);
    if(ds != null && isPermitted(Permissions.readDatasource(name)) == false) throw new NoSuchDatasourceException(name);
    return ds;
  }

  public Set<Datasource> getDatasources() {
    return ImmutableSet.copyOf(Iterables.filter(delegate.getDatasources(), Predicates.readableDatasource()));
  }

  public Datasource getTransientDatasourceInstance(String uid) {
    return delegate.getTransientDatasourceInstance(uid);
  }

  public boolean hasDatasource(String name) {
    return delegate.hasDatasource(name) && isPermitted(Permissions.readDatasource(name));
  }

  public boolean hasTransientDatasource(String uid) {
    return delegate.hasTransientDatasource(uid);
  }

  public void removeDatasource(Datasource datasource) {
    delegate.removeDatasource(datasource);
  }

  public void removeTransientDatasource(String uid) {
    delegate.removeTransientDatasource(uid);
  }

  private boolean isPermitted(String permission) {
    return authorizer.isPermitted(permission);
  }

}

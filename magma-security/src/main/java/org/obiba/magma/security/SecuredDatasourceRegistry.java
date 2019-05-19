/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.security;

import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.DatasourceRegistry;
import org.obiba.magma.Decorator;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.security.permissions.Permissions;
import org.obiba.magma.support.Decorators;
import org.obiba.magma.support.ValueTableReference;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class SecuredDatasourceRegistry implements DatasourceRegistry {

  private final Authorizer authorizer;

  private final DatasourceRegistry delegate;

  private final SecuredDatasourceDecorator securedDatasourceDecorator;

  public SecuredDatasourceRegistry(Authorizer authorizer, DatasourceRegistry datasourceRegistry) {
    if(authorizer == null) throw new IllegalArgumentException("authorizer cannot be null");
    if(datasourceRegistry == null) throw new IllegalArgumentException("datasourceRegistry cannot be null");
    this.authorizer = authorizer;
    delegate = datasourceRegistry;
    securedDatasourceDecorator = new SecuredDatasourceDecorator(authorizer);
  }

  @Override
  public ValueTableReference createReference(String reference) {
    return new SudoValueTableReference(authorizer, reference);
  }

  @Override
  public Datasource addDatasource(Datasource datasource) {
    return delegate.addDatasource(datasource);
  }

  @Override
  public Datasource addDatasource(DatasourceFactory factory) {
    return delegate.addDatasource(factory);
  }

  @Override
  public void addDecorator(Decorator<Datasource> decorator) {
    delegate.addDecorator(decorator);
  }

  @Override
  public String addTransientDatasource(DatasourceFactory factory) {
    return delegate.addTransientDatasource(factory);
  }

  @Override
  public Datasource getDatasource(String name) throws NoSuchDatasourceException {
    Datasource ds = delegate.getDatasource(name);
    if(ds != null && !isPermitted(Permissions.DatasourcePermissionBuilder.forDatasource(name).read().build()))
      throw new NoSuchDatasourceException(name);
    return securedDatasourceDecorator.decorate(ds);
  }

  @Override
  public Set<Datasource> getDatasources() {
    return ImmutableSet.copyOf(Iterables.transform(Sets.filter(delegate.getDatasources(),
        Permissions.DatasourcePermissionBuilder.forDatasource().read().asPredicate(authorizer)),
        Decorators.decoratingFunction(securedDatasourceDecorator)));
  }

  @Override
  public Datasource getTransientDatasourceInstance(String uid) {
    return delegate.getTransientDatasourceInstance(uid);
  }

  @Override
  public boolean hasDatasource(String name) {
    return delegate.hasDatasource(name) &&
        isPermitted(Permissions.DatasourcePermissionBuilder.forDatasource(name).read().build());
  }

  @Override
  public boolean hasTransientDatasource(String uid) {
    return delegate.hasTransientDatasource(uid);
  }

  @Override
  public void removeDatasource(Datasource datasource) {
    delegate.removeDatasource(securedDatasourceDecorator.undecorate(datasource));
  }

  @Override
  public void removeTransientDatasource(@Nullable String uid) {
    delegate.removeTransientDatasource(uid);
  }

  private boolean isPermitted(String permission) {
    return authorizer.isPermitted(permission);
  }

}

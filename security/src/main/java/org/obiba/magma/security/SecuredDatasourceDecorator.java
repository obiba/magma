package org.obiba.magma.security;

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;

public class SecuredDatasourceDecorator implements Decorator<Datasource> {

  private final Authorizer authorizer;

  public SecuredDatasourceDecorator(Authorizer authorizer) {
    this.authorizer = authorizer;
  }

  @Override
  public Datasource decorate(Datasource datasource) {
    return new SecuredDatasource(authorizer, datasource);
  }

}

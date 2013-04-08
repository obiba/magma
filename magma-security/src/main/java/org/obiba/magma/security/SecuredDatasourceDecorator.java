package org.obiba.magma.security;

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;

public class SecuredDatasourceDecorator implements Decorator<Datasource> {

  private final Authorizer authorizer;

  public SecuredDatasourceDecorator(Authorizer authorizer) {
    if(authorizer == null) throw new IllegalArgumentException("authorizer cannot be null");
    this.authorizer = authorizer;
  }

  @Override
  public Datasource decorate(Datasource datasource) {
    if(datasource == null) throw new IllegalArgumentException("datasource cannot be null");
    return new SecuredDatasource(authorizer, datasource);
  }

  public Datasource undecorate(Datasource datasource) {
    if(datasource instanceof SecuredDatasource) {
      SecuredDatasource secured = (SecuredDatasource) datasource;
      return secured.getWrappedDatasource();
    }
    return datasource;
  }

}

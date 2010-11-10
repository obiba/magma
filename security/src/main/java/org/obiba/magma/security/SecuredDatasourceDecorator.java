package org.obiba.magma.security;

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;

public class SecuredDatasourceDecorator implements Decorator<Datasource> {

  @Override
  public Datasource decorate(Datasource datasource) {
    return new SecuredDatasource(datasource);
  }

}

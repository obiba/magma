package org.obiba.magma.security;

import org.obiba.magma.DatasourceRegistry;
import org.obiba.magma.Decorator;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.magma.support.Initialisables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagmaSecurityExtension implements MagmaEngineExtension {

  private static final Logger log = LoggerFactory.getLogger(MagmaSecurityExtension.class);

  private final Authorizer authorizer = new ShiroAuthorizer();

  @Override
  public String getName() {
    return "security";
  }

  public Authorizer getAuthorizer() {
    return authorizer;
  }

  @Override
  public void initialise() {
    Initialisables.initialise(authorizer);
    MagmaEngine.get().decorate(new Decorator<DatasourceRegistry>() {

      @Override
      public DatasourceRegistry decorate(DatasourceRegistry object) {
        return new SecuredDatasourceRegistry(authorizer, object);
      }

    });

  }

}

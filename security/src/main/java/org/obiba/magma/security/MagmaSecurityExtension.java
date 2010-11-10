package org.obiba.magma.security;

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.DatasourceRegistry;
import org.obiba.magma.Decorator;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagmaSecurityExtension implements MagmaEngineExtension, Authorizer {

  private static final Logger log = LoggerFactory.getLogger(MagmaSecurityExtension.class);

  @Override
  public String getName() {
    return "security";
  }

  @Override
  public void initialise() {
    MagmaEngine.get().decorate(new Decorator<DatasourceRegistry>() {

      @Override
      public DatasourceRegistry decorate(DatasourceRegistry object) {
        return new SecuredDatasourceRegistry(MagmaSecurityExtension.this, object);
      }

    });

  }

  public boolean isPermitted(String permission) {
    boolean p = SecurityUtils.getSubject().isPermitted(permission);
    log.info("isPermitted({})=={}", permission, p);
    return p;
  }
}

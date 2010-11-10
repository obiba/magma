package org.obiba.magma.security;

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.DatasourceRegistry;
import org.obiba.magma.Decorator;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;

public class MagmaSecurityExtension implements MagmaEngineExtension, Authorizer {

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
    return SecurityUtils.getSubject().isPermitted(permission);
  }

}

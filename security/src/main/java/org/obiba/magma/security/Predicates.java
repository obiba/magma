package org.obiba.magma.security;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;

import com.google.common.base.Predicate;

public class Predicates {

  private static boolean isPermitted(String permission) {
    return MagmaEngine.get().getExtension(MagmaSecurityExtension.class).isPermitted(permission);
  }

  public static Predicate<Datasource> readableDatasource() {
    return DatasourcePredicate.READABLE_DATASOURCE;
  }

  private static class DatasourcePredicate implements Predicate<Datasource> {

    private static final DatasourcePredicate READABLE_DATASOURCE = new DatasourcePredicate("read");

    private final String action;

    private DatasourcePredicate(String action) {
      this.action = action;
    }

    @Override
    public boolean apply(Datasource input) {
      return isPermitted(Permissions.datasourcePermission(action, input.getName()));
    }

  }
}

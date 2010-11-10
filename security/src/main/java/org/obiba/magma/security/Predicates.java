package org.obiba.magma.security;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.security.Permissions.Domains;

import com.google.common.base.Predicate;

public class Predicates {

  private static boolean isPermitted(String permission) {
    return MagmaEngine.get().getExtension(MagmaSecurityExtension.class).isPermitted(permission);
  }

  public static Predicate<Datasource> readableDatasource() {
    return DatasourcePredicate.READABLE_DATASOURCE;
  }

  public static Predicate<Variable> readableVariable(ValueTable table) {
    return VariablePredicate.READABLE_VARIABLE;
  }

  private static class VariablePredicate implements Predicate<Variable> {

    private static final VariablePredicate READABLE_VARIABLE = new VariablePredicate(Permissions.Builder.create().domain(Domains.DATASOURCE).read());

    private final Permissions.Builder builder;

    private VariablePredicate(Permissions.Builder builder) {
      this.builder = builder;
    }

    @Override
    public boolean apply(Variable input) {
      return isPermitted(builder.instance(input.getName()).build());
    }

  }

  private static class DatasourcePredicate implements Predicate<Datasource> {

    private static final DatasourcePredicate READABLE_DATASOURCE = new DatasourcePredicate(Permissions.Builder.create().domain(Domains.DATASOURCE).read());

    private final Permissions.Builder builder;

    private DatasourcePredicate(Permissions.Builder builder) {
      this.builder = builder;
    }

    @Override
    public boolean apply(Datasource input) {
      return isPermitted(builder.instance(input.getName()).build());
    }

  }
}

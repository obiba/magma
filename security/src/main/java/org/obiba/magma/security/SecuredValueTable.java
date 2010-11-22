package org.obiba.magma.security;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.security.permissions.Permissions;
import org.obiba.magma.security.permissions.Permissions.ValueTablePermissionBuilder;
import org.obiba.magma.support.AbstractValueTableWrapper;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class SecuredValueTable extends AbstractValueTableWrapper {

  private final Authorizer authz;

  private final SecuredDatasource securedDatasource;

  private final ValueTable table;

  public SecuredValueTable(Authorizer authorizer, SecuredDatasource securedDatasource, ValueTable table) {
    if(authorizer == null) throw new IllegalArgumentException("authorizer cannot be null");
    if(securedDatasource == null) throw new IllegalArgumentException("securedDatasource cannot be null");
    if(table == null) throw new IllegalArgumentException("table cannot be null");
    this.authz = authorizer;
    this.securedDatasource = securedDatasource;
    this.table = table;
  }

  @Override
  public Datasource getDatasource() {
    return securedDatasource;
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    Variable v = super.getVariable(name);
    if(isReadable(v)) {
      return v;
    }
    throw new NoSuchVariableException(table.getName(), name);
  }

  @Override
  public boolean hasVariable(String name) {
    return super.hasVariable(name) && isReadable(name);

  }

  @Override
  public Iterable<Variable> getVariables() {
    return Iterables.filter(super.getVariables(), new Predicate<Variable>() {

      @Override
      public boolean apply(Variable input) {
        return isReadable(input);
      }

    });
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return table;
  }

  private boolean isReadable(String name) {
    return authz.isPermitted(builder().variable(name).build());
  }

  private boolean isReadable(Variable variable) {
    return isReadable(variable.getName());
  }

  private ValueTablePermissionBuilder builder() {
    return Permissions.ValueTablePermissionBuilder.forValueTable(this.table);
  }
}

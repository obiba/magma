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

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.security.permissions.Permissions;
import org.obiba.magma.security.permissions.Permissions.ValueTablePermissionBuilder;
import org.obiba.magma.support.AbstractValueTableWrapper;

import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SecuredValueTable extends AbstractValueTableWrapper {

  private final Authorizer authz;

  private final SecuredDatasource securedDatasource;

  private final ValueTable table;

  public SecuredValueTable(Authorizer authorizer, SecuredDatasource securedDatasource, ValueTable table) {
    if (authorizer == null) throw new IllegalArgumentException("authorizer cannot be null");
    if (securedDatasource == null) throw new IllegalArgumentException("securedDatasource cannot be null");
    if (table == null) throw new IllegalArgumentException("table cannot be null");
    authz = authorizer;
    this.securedDatasource = securedDatasource;
    this.table = table;
  }

  @NotNull
  @Override
  public Datasource getDatasource() {
    return securedDatasource;
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    Variable v = super.getVariable(name);
    if (isReadable(v)) {
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
    return StreamSupport.stream(super.getVariables().spliterator(), false)
        .filter(this::isReadable)
        .collect(Collectors.toList());
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return table;
  }

  private boolean isReadable(String name) {
    return authz.isPermitted(builder().variable(name).read().build());
  }

  private boolean isReadable(Variable variable) {
    return isReadable(variable.getName());
  }

  private ValueTablePermissionBuilder builder() {
    return Permissions.ValueTablePermissionBuilder.forValueTable(table);
  }

}

package org.obiba.magma.security;

import java.util.concurrent.Callable;

import org.obiba.magma.ValueTable;
import org.obiba.magma.security.permissions.Permissions;
import org.obiba.magma.support.ValueTableReference;

/**
 * An implementation of {@link ValueTableReference} that uses super user privileges to access the referenced table.
 * <p>
 * Note that this implementation will also remove the {@link SecuredValueTable} decorator such that the returned table
 * is no longer secured.
 */
public class SudoValueTableReference extends ValueTableReference {

  private final Authorizer authz;

  private final String permission;

  public SudoValueTableReference(Authorizer authz, String reference) {
    super(reference);
    this.authz = authz;
    this.permission = Permissions.DatasourcePermissionBuilder.forDatasource(getResolver().getDatasourceName()).table(getResolver().getTableName()).read().build();
  }

  @Override
  public ValueTable getWrappedValueTable() {
    if(authz.isPermitted(permission)) {
      return super.getWrappedValueTable();
    }
    // this subject is not allowed to dereference the table. Try to get super user privileges.
    return authz.silentSudo(new Callable<ValueTable>() {

      @Override
      public ValueTable call() throws Exception {
        return unwrap(getResolver().resolveTable());
      }
    });
  }

  protected ValueTable unwrap(ValueTable table) {
    if(table instanceof SecuredValueTable) {
      return ((SecuredValueTable) table).getWrappedValueTable();
    }
    return table;
  }

}

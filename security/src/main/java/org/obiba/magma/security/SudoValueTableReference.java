package org.obiba.magma.security;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import org.apache.shiro.SecurityUtils;
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

    // First look in the user's session. Maybe we already dereferenced this ValueTable
    ValueTable valueTable = lookInSession();
    if(valueTable == null) {
      valueTable = sudo();
      storeInSession(valueTable);
    }
    return valueTable;
  }

  protected void storeInSession(ValueTable valueTable) {
    SecurityUtils.getSubject().getSession().setAttribute(getReference(), new WeakReference<ValueTable>(valueTable));
  }

  protected ValueTable lookInSession() {
    WeakReference<ValueTable> ref = (WeakReference<ValueTable>) SecurityUtils.getSubject().getSession().getAttribute(getReference());
    if(ref != null) {
      return ref.get();
    }
    return null;
  }

  /**
   * Escalates user privileges to obtain the referenced valueTable. If successful, the result is an unsecured
   * ValueTable.
   * @return
   */
  protected ValueTable sudo() {
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

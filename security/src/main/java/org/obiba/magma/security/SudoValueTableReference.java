package org.obiba.magma.security;

import java.util.concurrent.Callable;

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.ValueTableReference;

/**
 * An implementation of {@link ValueTableReference} that super user privileges to access the referenced table.
 * <p>
 * Note that this implementation will also remove the {@link SecuredValueTable} decorator such that the returned table
 * is no longer secured.
 */
public class SudoValueTableReference extends ValueTableReference {

  private final Authorizer authz;

  public SudoValueTableReference(Authorizer authz, String reference) {
    super(reference);
    this.authz = authz;
  }

  @Override
  public ValueTable getWrappedValueTable() {
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

package org.obiba.magma.security;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.security.permissions.Permissions;
import org.obiba.magma.security.permissions.Permissions.DatasourcePermissionBuilder;
import org.obiba.magma.support.AbstractDatasourceWrapper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class SecuredDatasource extends AbstractDatasourceWrapper {

  private final Authorizer authz;

  public SecuredDatasource(Authorizer authorizer, Datasource datasource) {
    super(datasource);
    if(authorizer == null) throw new IllegalArgumentException("authorizer cannot be null");
    this.authz = authorizer;
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    ValueTable table = getWrappedDatasource().getValueTable(name);
    if(table != null && authzReadTable(name) == false) throw new NoSuchValueTableException(getName(), name);
    return new SecuredValueTable(authz, this, table);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return ImmutableSet.copyOf(Iterables.transform(Iterables.filter(getWrappedDatasource().getValueTables(), builder().tables().read().asPredicate(authz)), new Function<ValueTable, ValueTable>() {

      @Override
      public ValueTable apply(ValueTable from) {
        return new SecuredValueTable(authz, SecuredDatasource.this, from);
      }
    }));
  }

  @Override
  public boolean hasValueTable(String name) {
    return getWrappedDatasource().hasValueTable(name) && authzReadTable(name);
  }

  @Override
  public boolean canDropTable(String name) {
    return getWrappedDatasource().canDropTable(name) && authzDropTable(name);
  }

  @Override
  public void dropTable(String name) {
    if(hasValueTable(name) == true) {
      if(authzDropTable(name) == false) {
        throw new MagmaRuntimeException("not authorized to drop table " + getName() + "." + name);
      } else {
        getWrappedDatasource().dropTable(name);
      }
    }
  }

  protected boolean authzReadTable(String name) {
    return authz.isPermitted(builder().table(name).read().build());
  }

  protected boolean authzDropTable(String name) {
    return authz.isPermitted(builder().table(name).delete().build());
  }

  private DatasourcePermissionBuilder builder() {
    return Permissions.DatasourcePermissionBuilder.forDatasource(getWrappedDatasource());
  }
}

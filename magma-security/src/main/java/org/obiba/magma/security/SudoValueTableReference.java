package org.obiba.magma.security;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.apache.shiro.subject.ExecutionException;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.support.ValueTableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link ValueTableReference} that uses super user privileges to access the referenced table.
 * <p/>
 * Note that this implementation will also remove the {@link SecuredValueTable} decorator such that the returned table
 * is no longer secured.
 */
public class SudoValueTableReference extends ValueTableReference {

  private static final Logger log = LoggerFactory.getLogger(SudoValueTableReference.class);

  private final Authorizer authz;

  private Datasource wrappedDatasource;

  public SudoValueTableReference(Authorizer authz, String reference) {
    super(reference);
    this.authz = authz;
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return sudo();
  }

  /**
   * Escalates user privileges to obtain the referenced valueTable. If successful, the result is an unsecured
   * ValueTable.
   *
   * @return
   */
  protected ValueTable sudo() {
    try {
      if(wrappedDatasource == null) {
        wrappedDatasource = authz.silentSudo(new DatasourceCallable());
      }
      return wrappedDatasource.getValueTable(getResolver().getTableName());
    } catch(NoSuchValueTableException e1) {
      log.error("No such value table: {}. {}", getReference(), e1.getMessage());
      return getDummyValueTable(wrappedDatasource);
    } catch(RuntimeException e2) {
      log.error("No such datasource for value table: {}. {}", getReference(), e2.getMessage());
      Datasource ds = new StaticDatasource(getResolver().getDatasourceName());
      return getDummyValueTable(ds);
    }
  }

  // OPAL-1821
  private ValueTable getDummyValueTable(Datasource ds) {
    return new StaticValueTable(ds, getResolver().getTableName(), new ArrayList<String>(), "?");
  }

  private class DatasourceCallable implements Callable<Datasource> {
    @Override
    public Datasource call() throws Exception {
      return unwrap(MagmaEngine.get().getDatasource(getResolver().getDatasourceName()));
    }

    private Datasource unwrap(Datasource ds) {
      return ds instanceof SecuredDatasource ? ((AbstractDatasourceWrapper) ds).getWrappedDatasource() : ds;
    }
  }

}

package org.obiba.magma.support;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

/**
 *
 */
public class BatchDatasource extends AbstractDatasourceWrapperWithCachedTables {

  private final int limit;

  public BatchDatasource(Datasource wrapped, int limit) {
    super(wrapped);
    this.limit = limit;
  }

  @Override
  protected ValueTable createValueTable(ValueTable table) {
    return new BatchValueTable(table, limit);
  }

}

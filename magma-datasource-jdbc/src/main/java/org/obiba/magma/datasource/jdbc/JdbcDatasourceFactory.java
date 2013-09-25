package org.obiba.magma.datasource.jdbc;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;

public class JdbcDatasourceFactory extends AbstractDatasourceFactory {

  private JdbcDatasourceSettings datasourceSettings;

  private DataSource dataSource;

  @Nonnull
  @Override
  public Datasource internalCreate() {
    return new JdbcDatasource(getName(), dataSource, datasourceSettings);
  }

  public void setDataSource(@Nonnull DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setDatasourceSettings(@Nonnull JdbcDatasourceSettings datasourceSettings) {
    this.datasourceSettings = datasourceSettings;
  }
}

package org.obiba.magma.datasource.jdbc;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;

public class JdbcDatasourceFactory extends AbstractDatasourceFactory {

  private JdbcDatasourceSettings datasourceSettings;

  private DataSource dataSource;

  @NotNull
  @Override
  public Datasource internalCreate() {
    return new JdbcDatasource(getName(), dataSource, datasourceSettings);
  }

  public void setDataSource(@NotNull DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setDatasourceSettings(@NotNull JdbcDatasourceSettings datasourceSettings) {
    this.datasourceSettings = datasourceSettings;
  }
}

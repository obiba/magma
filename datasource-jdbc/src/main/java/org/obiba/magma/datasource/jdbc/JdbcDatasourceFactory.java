package org.obiba.magma.datasource.jdbc;

import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;

public class JdbcDatasourceFactory extends AbstractDatasourceFactory {
  //
  // Constants
  //

  public static final String DRIVER_CLASS_NAME = "driverClassName";

  public static final String URL = "url";

  public static final String USERNAME = "username";

  public static final String PASSWORD = "password";

  //
  // Instance Variables
  //

  private String name;

  private Properties jdbcProperties;

  private JdbcDatasourceSettings settings;

  //
  // AbstractDatasourceFactory Methods
  //

  @Override
  public Datasource create() {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(jdbcProperties.getProperty(DRIVER_CLASS_NAME));
    dataSource.setUrl(jdbcProperties.getProperty(URL));
    dataSource.setUsername(jdbcProperties.getProperty(USERNAME));
    dataSource.setPassword(jdbcProperties.getProperty(PASSWORD));

    return new JdbcDatasource(name, dataSource, settings);
  }

  //
  // Methods
  //

  public void setName(String name) {
    this.name = name;
  }

  public void setJdbcProperties(Properties jdbcProperties) {
    this.jdbcProperties = jdbcProperties;
  }

  public void setDatasourceSettings(JdbcDatasourceSettings settings) {
    this.settings = settings;
  }

}

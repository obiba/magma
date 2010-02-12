package org.obiba.magma.datasource.jdbc;

public class JdbcDatasourceFactory {

  private String driver;

  private String baseUrl;

  private String username;

  private String password;

  public JdbcDatasourceFactory(String driver, String baseUrl, String username, String password) {
    super();
    this.driver = driver;
    this.baseUrl = baseUrl;
    this.username = username;
    this.password = password;
  }

  public JdbcDatasource create(String name) {
    return null;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}

package org.obiba.magma.datasource.hibernate.support;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;

public class LocalSessionFactoryProvider implements SessionFactoryProvider, Initialisable {

  private String driver;

  private String baseUrl;

  private String username;

  private String password;

  private String dialect;

  private Properties properties;

  private SessionFactory sessionFactory;

  public LocalSessionFactoryProvider() {

  }

  public LocalSessionFactoryProvider(String driver, String baseUrl, String username, String password, String dialect) {
    this.driver = driver;
    this.baseUrl = baseUrl;
    this.username = username;
    this.password = password;
    this.dialect = dialect;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  @Override
  public void initialise() {
    AnnotationConfiguration cfg = new AnnotationConfiguration();

    new AnnotationConfigurationHelper().configure(cfg);

    // Set some reasonable defaults
    cfg.setProperty(Environment.HBM2DDL_AUTO, "update");
    cfg.setProperty(Environment.TRANSACTION_STRATEGY, "org.hibernate.transaction.JDBCTransactionFactory");
    cfg.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
    cfg.setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.EhCacheProvider");
    cfg.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);

    if(properties != null) {
      cfg.addProperties(properties);
    }

    cfg.setProperty(Environment.DRIVER, driver);
    cfg.setProperty(Environment.URL, baseUrl);
    cfg.setProperty(Environment.USER, username);
    cfg.setProperty(Environment.PASS, password);
    cfg.setProperty(Environment.DIALECT, dialect);

    // we want to store byte[] as oid instead of bytea.
    // See http://in.relation.to/15492.lace
    if("org.hibernate.dialect.PostgreSQLDialect".equals(dialect)) {
      cfg.setProperty("hibernate.jdbc.use_streams_for_binary", "false");
    }

    sessionFactory = cfg.buildSessionFactory();
  }

  @Override
  public SessionFactory getSessionFactory() {
    if(sessionFactory == null) {
      throw new MagmaRuntimeException("Call initialise first.");
    }
    return sessionFactory;
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

  public void setDialect(String dialect) {
    this.dialect = dialect;
  }

}

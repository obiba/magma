package org.obiba.magma.datasource.jpa;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.obiba.magma.datasource.jpa.domain.CategoryState;
import org.obiba.magma.datasource.jpa.domain.DatasourceState;
import org.obiba.magma.datasource.jpa.domain.ValueSetState;
import org.obiba.magma.datasource.jpa.domain.ValueSetValue;
import org.obiba.magma.datasource.jpa.domain.ValueTableState;
import org.obiba.magma.datasource.jpa.domain.VariableEntityState;
import org.obiba.magma.datasource.jpa.domain.VariableState;
import org.obiba.magma.datasource.jpa.domain.attribute.AttributeAwareAdapter;
import org.obiba.magma.datasource.jpa.domain.attribute.JPAAttribute;

public class JPADatasourceFactory {

  private String driver;

  private String baseUrl;

  private String username;

  private String password;

  private String dialect;

  public JPADatasourceFactory(String driver, String baseUrl, String username, String password, String dialect) {
    super();
    this.driver = driver;
    this.baseUrl = baseUrl;
    this.username = username;
    this.password = password;
    this.dialect = dialect;
  }

  public JPADatasource create(String name) {
    AnnotationConfiguration cfg = new AnnotationConfiguration();

    cfg.addAnnotatedClass(DatasourceState.class);
    cfg.addAnnotatedClass(VariableEntityState.class);
    cfg.addAnnotatedClass(ValueTableState.class);
    cfg.addAnnotatedClass(ValueSetState.class);
    cfg.addAnnotatedClass(ValueSetValue.class);
    cfg.addAnnotatedClass(VariableState.class);
    cfg.addAnnotatedClass(CategoryState.class);
    cfg.addAnnotatedClass(JPAAttribute.class);
    cfg.addAnnotatedClass(AttributeAwareAdapter.class);

    cfg.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);

    cfg.setProperty("hibernate.hbm2ddl.auto", "update");

    cfg.setProperty("hibernate.connection.driver_class", driver);
    cfg.setProperty("hibernate.connection.url", baseUrl);
    cfg.setProperty("hibernate.connection.username", username);
    cfg.setProperty("hibernate.connection.password", password);
    cfg.setProperty("hibernate.dialect", dialect);

    cfg.setProperty("hibernate.current_session_context_class", "thread");

    // Obviously, we don't want to do this... But Datasource isn't transaction-aware. Do we expect the transactions to
    // be demarcated outside of the Datasource?
    JPADatasource ds = new JPADatasource(name, cfg.buildSessionFactory()) {

      @Override
      public void initialise() {
        getSessionFactory().getCurrentSession().beginTransaction();
        super.initialise();
      }

      @Override
      public void dispose() {
        super.dispose();
        getSessionFactory().getCurrentSession().getTransaction().commit();
        getSessionFactory().getCurrentSession().close();
        getSessionFactory().close();
      }
    };

    return ds;
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

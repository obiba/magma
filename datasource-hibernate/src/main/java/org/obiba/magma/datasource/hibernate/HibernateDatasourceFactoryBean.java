package org.obiba.magma.datasource.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;

public class HibernateDatasourceFactoryBean implements FactoryBean {

  private String name;

  private SessionFactory sessionFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Object getObject() throws Exception {
    return new HibernateDatasource(name, sessionFactory);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class getObjectType() {
    return HibernateDatasource.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

}

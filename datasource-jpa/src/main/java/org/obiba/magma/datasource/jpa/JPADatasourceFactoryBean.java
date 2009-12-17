package org.obiba.magma.datasource.jpa;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;

public class JPADatasourceFactoryBean implements FactoryBean {

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
    return new JPADatasource(name, sessionFactory);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class getObjectType() {
    return JPADatasource.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

}

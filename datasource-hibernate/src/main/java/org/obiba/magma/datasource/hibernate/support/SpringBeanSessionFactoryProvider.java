package org.obiba.magma.datasource.hibernate.support;

import org.hibernate.SessionFactory;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringBeanSessionFactoryProvider implements SessionFactoryProvider {

  @Autowired
  private BeanFactory beanFactory;

  private String beanName;

  @Override
  public SessionFactory getSessionFactory() {
    if(beanFactory == null) throw new NullPointerException("beanFactory cannot be null");
    if(beanName == null) throw new NullPointerException("beanName cannot be null");
    return (SessionFactory) beanFactory.getBean(beanName, SessionFactory.class);
  }

}

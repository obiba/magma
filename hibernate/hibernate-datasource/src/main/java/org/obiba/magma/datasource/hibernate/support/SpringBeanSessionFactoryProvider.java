package org.obiba.magma.datasource.hibernate.support;

import org.hibernate.SessionFactory;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringBeanSessionFactoryProvider implements SessionFactoryProvider {

  private String beanName;

  @Autowired
  private transient BeanFactory beanFactory;

  // Public ctor for XStream de-ser.
  public SpringBeanSessionFactoryProvider() {

  }

  public SpringBeanSessionFactoryProvider(BeanFactory beanFactory, String beanName) {
    if(beanFactory == null) throw new IllegalArgumentException("beanFactory cannot be null");
    if(beanName == null) throw new IllegalArgumentException("beanName cannot be null");
    this.beanFactory = beanFactory;
    this.beanName = beanName;
  }

  @Override
  public SessionFactory getSessionFactory() {
    if(beanFactory == null) throw new IllegalArgumentException("beanFactory cannot be null");
    if(beanName == null) throw new IllegalArgumentException("beanName cannot be null");
    return (SessionFactory) beanFactory.getBean(beanName, SessionFactory.class);
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public String getBeanName() {
    return beanName;
  }
}

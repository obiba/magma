/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.support;

import org.hibernate.SessionFactory;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringBeanSessionFactoryProvider implements SessionFactoryProvider {

  private String beanName;

  @Autowired
  @SuppressWarnings("TransientFieldInNonSerializableClass")
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
    return beanFactory.getBean(beanName, SessionFactory.class);
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  public String getBeanName() {
    return beanName;
  }
}

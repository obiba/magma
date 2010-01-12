package org.obiba.magma.datasource.hibernate;

import org.hibernate.SessionFactory;

/**
 * Strategy for obtaining the Hibernate {@code SessionFactory} instance.
 * @see HibernateDatasourceManager
 */
public interface SessionFactoryProvider {

  public SessionFactory getSessionFactory();

}

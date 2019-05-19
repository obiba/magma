/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.hibernate.support;

import java.util.Properties;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.datasource.hibernate.SessionFactoryProvider;
import org.obiba.magma.datasource.hibernate.cfg.HibernateConfigurationHelper;
import org.obiba.magma.datasource.hibernate.cfg.MagmaDialectResolver;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

public class LocalSessionFactoryProvider implements SessionFactoryProvider, Initialisable {

  private DataSource dataSource;

  private String dialect;

  private Properties properties;

  private SessionFactory sessionFactory;

  private Object jtaTransactionManager;

  public LocalSessionFactoryProvider() {

  }

  public LocalSessionFactoryProvider(@NotNull DataSource dataSource, @Nullable String dialect) {
    this.dataSource = dataSource;
    this.dialect = dialect;
  }

  @Override
  public void initialise() {

    // Set some reasonable defaults
    LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
    builder.addAnnotatedClasses(HibernateConfigurationHelper.getAnnotatedTypesAsArray());
    builder.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
    builder.setProperty(Environment.DIALECT, dialect);
    builder.setProperty(Environment.HBM2DDL_AUTO, "update");
    builder.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
    builder.setProperty(Environment.USE_QUERY_CACHE, "true");
    builder.setProperty(Environment.CACHE_REGION_FACTORY, "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory");
    builder.setProperty(Environment.DIALECT_RESOLVERS, MagmaDialectResolver.class.getName());
    if(jtaTransactionManager != null) builder.setJtaTransactionManager(jtaTransactionManager);

    // we want to store byte[] as oid instead of bytea.
    // See http://in.relation.to/15492.lace
    if(dialect.startsWith("org.hibernate.dialect.PostgreSQL")) {
      builder.setProperty(Environment.USE_STREAMS_FOR_BINARY, "false");
    }

    if(properties != null) builder.addProperties(properties);
    sessionFactory = builder.buildSessionFactory();
  }

  @Override
  public SessionFactory getSessionFactory() {
    if(sessionFactory == null) {
      throw new MagmaRuntimeException("Call initialise first.");
    }
    return sessionFactory;
  }

  public void setDialect(String dialect) {
    this.dialect = dialect;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public void setJtaTransactionManager(Object jtaTransactionManager) {
    this.jtaTransactionManager = jtaTransactionManager;
  }

}

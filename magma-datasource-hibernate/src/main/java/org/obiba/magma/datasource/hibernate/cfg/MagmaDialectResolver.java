/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma.datasource.hibernate.cfg;

import org.hibernate.dialect.Database;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

/**
 * Ensures usage of InnoDB for MySQL databases and uses custom dialect for HSQLDB, otherwise, fallback to default
 * behavior.
 * <p/>
 * This class is instantiated by Hibernate itself through the {@code hibernate.properties} file.
 */
public class MagmaDialectResolver implements DialectResolver {

  private static final long serialVersionUID = 6167226108895659666L;

  @Override
  @SuppressWarnings("ChainOfInstanceofChecks")
  public Dialect resolveDialect(DialectResolutionInfo info) {
    Dialect dialect = resolveStandardDialect(info);
    if(dialect instanceof MySQL5Dialect) return new MySQL5InnoDbUtf8Dialect();
    if(dialect instanceof HSQLDialect) return new MagmaHSQLDialect();
    return dialect;
  }

  private Dialect resolveStandardDialect(DialectResolutionInfo info) {
    for ( Database database : Database.values() ) {
      Dialect dialect = database.resolveDialect( info );
      if ( dialect != null ) {
        return dialect;
      }
    }
    return null;
  }

}

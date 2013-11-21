package org.obiba.magma.datasource.hibernate.cfg;

import java.sql.Types;

import org.hibernate.dialect.HSQLDialect;

/**
 * Overrides the HSQLDialect to force the use of longvarchar for clobs
 */
public class MagmaHSQLDialect extends HSQLDialect {

  public MagmaHSQLDialect() {
    // Force the use of longvarchar and longvarbinary for clobs/blobs
    registerColumnType(Types.BLOB, "longvarbinary");
    registerColumnType(Types.CLOB, "longvarchar");
  }
}

package org.obiba.magma.datasource.jdbc.support;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import liquibase.database.Database;
import liquibase.sql.visitor.AbstractSqlVisitor;

/**
 * Modifies occurrences of "BLOB" to vendor-specific types.
 */
public class BlobTypeVisitor extends AbstractSqlVisitor {

  @Override
  public String modifySql(String sql, Database database) {
    if("mysql".equals(database.getShortName())) {
      return sql.replaceAll("BLOB", "LONGBLOB");
    }

    return sql;
  }

  @Override
  public String getName() {
    return BlobTypeVisitor.class.getSimpleName();
  }

  @Override
  public Set<String> getApplicableDbms() {
    return ImmutableSet.of("mysql");
  }
}

package org.obiba.magma.datasource.jdbc.support;

import java.util.Collection;

import liquibase.database.Database;
import liquibase.database.sql.visitor.SqlVisitor;

/**
 * Modifies occurrences of "BLOB" to vendor-specific types.
 */
public class BlobTypeVisitor implements SqlVisitor {
  //
  // SqlVisitor Methods
  //

  @Override
  public String getTagName() {
    return BlobTypeVisitor.class.getSimpleName();
  }

  @SuppressWarnings("unchecked")
  public void setApplicableDbms(Collection applicableDbms) {
    // no-op
  }

  public boolean isApplicable(Database database) {
    if(database.getTypeName().equals("mysql")) {
      return true;
    }
    return false;
  }

  @Override
  public String modifySql(String sql, Database database) {
    if(database.getTypeName().equals("mysql")) {
      return sql.replaceAll("BLOB", "LONGBLOB");
    }
    return sql;
  }
}

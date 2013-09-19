package org.obiba.magma.datasource.hibernate;

import org.hibernate.dialect.HSQLDialect;

/**
 * Workaround for schemaExport ERROR when using in-memory database (H2) - drop constraint issue HHH000389
 * See https://hibernate.atlassian.net/browse/HHH-7002
 */
public class InMemoryHSQLDialect extends HSQLDialect {

  @Override
  public String getDropSequenceString(String sequenceName) {
    // Adding the "if exists" clause to avoid warnings
    return "drop sequence if exists " + sequenceName;
  }

  @Override
  public boolean dropConstraints() {
    // We don't need to drop constraints before dropping tables, that just leads to error
    // messages about missing tables when we don't have a schema in the database
    return false;
  }

}

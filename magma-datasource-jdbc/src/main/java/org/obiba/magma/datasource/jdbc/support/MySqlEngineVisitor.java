package org.obiba.magma.datasource.jdbc.support;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import liquibase.database.Database;
import liquibase.sql.visitor.AbstractSqlVisitor;

public class MySqlEngineVisitor extends AbstractSqlVisitor {

  @Override
  public String modifySql(String sql, Database database) {
    if("mysql".equals(database.getShortName()) && sql.toLowerCase().startsWith("create table") &&
        !sql.toLowerCase().contains("engine=")) {
      return sql + "ENGINE=InnoDB";
    }

    return sql;
  }

  @Override
  public String getName() {
    return MySqlEngineVisitor.class.getSimpleName();
  }

  @Override
  public Set<String> getApplicableDbms() {
    return ImmutableSet.of("mysql");
  }
}

package org.obiba.magma.datasource.jdbc.support;

import com.google.common.collect.ImmutableSet;

import liquibase.database.Database;
import liquibase.sql.visitor.AbstractSqlVisitor;

public class MySqlEngineVisitor extends AbstractSqlVisitor {

  public MySqlEngineVisitor() {
    setApplicableDbms(ImmutableSet.of("mysql"));
  }

  @Override
  public String modifySql(String sql, Database database) {
    if(sql.toLowerCase().startsWith("create table") && !sql.toLowerCase().contains("engine="))
      sql = sql + "ENGINE=InnoDB";

    if(sql.contains("BLOB")) sql = sql.replaceAll("BLOB", "LONGBLOB");

    return sql;
  }

  @Override
  public String getName() {
    return MySqlEngineVisitor.class.getSimpleName();
  }
}

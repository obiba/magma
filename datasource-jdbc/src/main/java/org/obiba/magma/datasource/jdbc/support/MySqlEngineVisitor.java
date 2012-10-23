package org.obiba.magma.datasource.jdbc.support;

import java.util.Collection;

import liquibase.database.Database;
import liquibase.database.sql.visitor.SqlVisitor;

public class MySqlEngineVisitor implements SqlVisitor {

  @Override
  public String getTagName() {
    return MySqlEngineVisitor.class.getSimpleName();
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
    if(sql.toLowerCase().startsWith("create table") && sql.toLowerCase().contains("engine=") == false) {
      return sql + "ENGINE=InnoDB";
    }
    return sql;
  }
}

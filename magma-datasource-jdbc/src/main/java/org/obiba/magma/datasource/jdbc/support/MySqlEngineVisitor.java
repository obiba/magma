/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    StringBuilder sb = new StringBuilder(sql);

    if(sql.toLowerCase().startsWith("create table") && !sql.toLowerCase().contains("engine="))
      sb.append("ENGINE=InnoDB");

    if(sql.contains("BLOB")) return sb.toString().replaceAll("BLOB", "LONGBLOB");

    return sb.toString();
  }

  @Override
  public String getName() {
    return MySqlEngineVisitor.class.getSimpleName();
  }
}

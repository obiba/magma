/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.jdbc;

import org.obiba.magma.Initialisable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.jdbc.core.RowMapper;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class JdbcVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

  private final JdbcValueTable valueTable;

  private Set<VariableEntity> entities = new LinkedHashSet<>();

  JdbcVariableEntityProvider(JdbcValueTable valueTable) {
    super(valueTable.getEntityType());
    this.valueTable = valueTable;
  }

  @Override
  public void initialise() {
    JdbcDatasource datasource = valueTable.getDatasource();
    entities = new LinkedHashSet<>();
    List<VariableEntity> results = datasource.getJdbcTemplate().query(String
            .format("SELECT %s FROM %s", valueTable.getEntityIdentifierColumnSql(),
                datasource.escapeTableName(valueTable.getSqlName())),
        (rs, rowNum) -> new VariableEntityBean(valueTable.getEntityType(), valueTable.extractEntityIdentifier(rs)));
    entities.addAll(results);
  }

  @NotNull
  @Override
  public Set<VariableEntity> getVariableEntities() {
    return Collections.unmodifiableSet(entities);
  }

}

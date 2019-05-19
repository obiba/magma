/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class JdbcVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

  private final JdbcValueTable valueTable;

  private Set<VariableEntity> entities = new LinkedHashSet<>();

  private boolean multilines = false;

  JdbcVariableEntityProvider(JdbcValueTable valueTable) {
    super(valueTable.getEntityType());
    this.valueTable = valueTable;
  }

  @Override
  public void initialise() {
    JdbcDatasource datasource = valueTable.getDatasource();
    String whereStatement = valueTable.getSettings().hasEntityIdentifiersWhere() ? "WHERE " + valueTable.getSettings().getEntityIdentifiersWhere() : "";
    entities = new LinkedHashSet<>();

    initialiseMultilines(datasource, whereStatement);

    // get the distinct list of entity identifiers
    List<VariableEntity> results = datasource.getJdbcTemplate().query(String
            .format("SELECT DISTINCT %s FROM %s %s",
                valueTable.getEntityIdentifierColumnSql(),
                datasource.escapeTableName(valueTable.getSqlName()),
                whereStatement),
        (rs, rowNum) -> new VariableEntityBean(valueTable.getEntityType(), valueTable.extractEntityIdentifier(rs)));
    entities.addAll(results);
  }

  @NotNull
  @Override
  public Set<VariableEntity> getVariableEntities() {
    return Collections.unmodifiableSet(entities);
  }

  public boolean isMultilines() {
    return multilines;
  }

  //
  // Private methods
  //

  /**
   * Detect if there are multiple lines per entity.
   *
   * @param datasource
   * @param whereStatement
   */
  private void initialiseMultilines(JdbcDatasource datasource, String whereStatement) {
    long count = datasource.getJdbcTemplate().queryForObject(String.format("SELECT COUNT(%s) FROM %s %s",
        valueTable.getEntityIdentifierColumnSql(),
        datasource.escapeTableName(valueTable.getSqlName()),
        whereStatement), Long.class);

    long distinctCount = datasource.getJdbcTemplate().queryForObject(String.format("SELECT COUNT(DISTINCT %s) FROM %s %s",
        valueTable.getEntityIdentifierColumnSql(),
        datasource.escapeTableName(valueTable.getSqlName()),
        whereStatement), Long.class);

    multilines = count > distinctCount;
  }

}

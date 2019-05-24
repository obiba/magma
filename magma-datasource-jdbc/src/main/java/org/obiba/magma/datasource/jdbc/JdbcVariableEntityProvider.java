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
import org.obiba.magma.lang.VariableEntityList;
import org.obiba.magma.support.AbstractVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

class JdbcVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

  private static final Logger log = LoggerFactory.getLogger(JdbcVariableEntityProvider.class);

  private final JdbcValueTable valueTable;

  private int entitiesCount = -1;

  private boolean multilines = false;

  JdbcVariableEntityProvider(JdbcValueTable valueTable) {
    super(valueTable.getEntityType());
    this.valueTable = valueTable;
  }

  @Override
  public synchronized void initialise() {
    if (entitiesCount == -1) {
      JdbcDatasource datasource = valueTable.getDatasource();
      String whereStatement = valueTable.getSettings().hasEntityIdentifiersWhere() ? "WHERE " + valueTable.getSettings().getEntityIdentifiersWhere() : "";
      int count = datasource.getJdbcTemplate().queryForObject(String.format("SELECT COUNT(%s) FROM %s %s",
          valueTable.getEntityIdentifierColumnSql(),
          datasource.escapeTableName(valueTable.getSqlName()),
          whereStatement), Integer.class);

      entitiesCount = datasource.getJdbcTemplate().queryForObject(String.format("SELECT COUNT(DISTINCT %s) FROM %s %s",
          valueTable.getEntityIdentifierColumnSql(),
          datasource.escapeTableName(valueTable.getSqlName()),
          whereStatement), Integer.class);

      multilines = count > entitiesCount;
    }
  }

  @NotNull
  @Override
  public List<VariableEntity> getVariableEntities() {
    log.debug("Querying all entities from Tabular SQL table {}!", valueTable.getName());
    return getVariableEntities(0, -1);
  }

  public List<VariableEntity> getVariableEntities(int offset, int limit) {
    initialise();
    int from = Math.max(offset, 0);
    from = Math.min(from, entitiesCount);
    int pageSize = limit < 0 ? entitiesCount : limit;

    List<VariableEntity> entities = new VariableEntityList();
    JdbcDatasource datasource = valueTable.getDatasource();
    String whereStatement = valueTable.getSettings().hasEntityIdentifiersWhere() ? "WHERE " + valueTable.getSettings().getEntityIdentifiersWhere() : "";
    String idColumn = valueTable.getEntityIdentifierColumnSql();
    String query = String
        .format("SELECT DISTINCT %s FROM %s %s ORDER BY %s ASC LIMIT %s OFFSET %s", // works for mysql, maria, posgre, hsql databases
            idColumn,
            datasource.escapeTableName(valueTable.getSqlName()),
            whereStatement,
            idColumn, pageSize, from);
    // get the distinct list of entity identifiers
    List<VariableEntity> results = datasource.getJdbcTemplate().query(query,
        (rs, rowNum) -> new VariableEntityBean(valueTable.getEntityType(), valueTable.extractEntityIdentifier(rs)));

    entities.addAll(results);
    return entities;
  }

  public int getVariableEntityCount() {
    if (entitiesCount == -1) initialise();
    return entitiesCount;
  }

  public boolean isMultilines() {
    return multilines;
  }

  //
  // Private methods
  //

  public void addAll(List<VariableEntity> entities) {
    entitiesCount = entitiesCount + entities.size(); // not sure this is correct when it is an update
  }

  public void remove(VariableEntity entity) {
    entitiesCount = entitiesCount - 1;
  }

}

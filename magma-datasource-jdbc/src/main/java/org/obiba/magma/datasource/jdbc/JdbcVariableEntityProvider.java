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

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

class JdbcVariableEntityProvider extends AbstractVariableEntityProvider implements Initialisable {

  private final JdbcValueTable valueTable;

  private final List<VariableEntity> entities = new VariableEntityList();

  private boolean multilines = false;

  JdbcVariableEntityProvider(JdbcValueTable valueTable) {
    super(valueTable.getEntityType());
    this.valueTable = valueTable;
  }

  @Override
  public synchronized void initialise() {
    if (entities.isEmpty()) {
      JdbcDatasource datasource = valueTable.getDatasource();
      String whereStatement = valueTable.getSettings().hasEntityIdentifiersWhere() ? "WHERE " + valueTable.getSettings().getEntityIdentifiersWhere() : "";

      // get the (non) distinct list of entity identifiers
      List<VariableEntity> results = datasource.getJdbcTemplate().query(String
              .format("SELECT %s FROM %s %s",
                  valueTable.getEntityIdentifierColumnSql(),
                  datasource.escapeTableName(valueTable.getSqlName()),
                  whereStatement),
          (rs, rowNum) -> new VariableEntityBean(valueTable.getEntityType(), valueTable.extractEntityIdentifier(rs)));

      entities.addAll(results);
      multilines = entities.size() < results.size();
    }
  }

  @NotNull
  @Override
  public List<VariableEntity> getVariableEntities() {
    return Collections.unmodifiableList(entities);
  }

  public boolean isMultilines() {
    return multilines;
  }

  //
  // Private methods
  //

  public void addAll(List<VariableEntity> entities) {
    this.entities.addAll(entities);
  }

  public void remove(VariableEntity entity) {
    entities.remove(entity);
  }
}

/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.csv;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityProvider;

public class CsvVariableEntityProvider implements VariableEntityProvider {

  @NotNull
  private final CsvValueTable valueTable;

  @NotNull
  private final String entityType;

  public CsvVariableEntityProvider(@NotNull CsvValueTable table, @NotNull String entityType) {
    valueTable = table;
    this.entityType = entityType;
  }

  @NotNull
  @Override
  public String getEntityType() {
    return entityType;
  }

  @NotNull
  @Override
  public Set<VariableEntity> getVariableEntities() {
    return valueTable.entities;
  }

  @Override
  public boolean isForEntityType(@SuppressWarnings("ParameterHidesMemberVariable") String entityType) {
    return getEntityType().equals(entityType);
  }

  public void add(VariableEntity entity) {
    if (!valueTable.entities.contains(entity)) valueTable.entities.add(entity);
  }
}

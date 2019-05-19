/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

public abstract class AbstractVariableEntityProvider implements VariableEntityProvider {

  @NotNull
  private final String entityType;

  protected AbstractVariableEntityProvider(@NotNull String entityType) {
    this.entityType = entityType;
  }

  @NotNull
  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public boolean isForEntityType(@SuppressWarnings("ParameterHidesMemberVariable") String entityType) {
    return getEntityType().equals(entityType);
  }

}

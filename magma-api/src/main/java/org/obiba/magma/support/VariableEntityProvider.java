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

import org.obiba.magma.VariableEntity;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Provider of entities.
 */
public interface VariableEntityProvider {

  /**
   * What the entities are about.
   *
   * @return
   */
  @NotNull
  String getEntityType();

  /**
   * Check it applies to the entity type.
   *
   * @param entityType
   * @return
   */
  boolean isForEntityType(String entityType);

  /**
   * Get the whole list of entities.
   *
   * @return
   */
  @NotNull
  List<VariableEntity> getVariableEntities();
}

/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma;

import java.util.List;

public class MagmaStoreExtension implements MagmaEngineExtension {

  private static final long serialVersionUID = -608961524433111119L;

  private transient VariableEntityStore entityStore;

  public MagmaStoreExtension() {
  }

  public MagmaStoreExtension(VariableEntityStore entityStore) {
    this.entityStore = entityStore;
  }

  @Override
  public String getName() {
    return "magma-store";
  }

  @Override
  public void initialise() {

  }

  public boolean hasVariableEntityStore() {
    return entityStore != null;
  }

  public VariableEntityStore getVariableEntityStore() {
    return entityStore;
  }

  /**
   * Interface to store and page a list of variable entities.
   */
  public interface VariableEntityStore {

    /**
     * Save and replace the entities for a table.
     *
     * @param table
     * @param entities
     */
    void saveVariableEntities(ValueTable table, List<VariableEntity> entities);

    /**
     * Get all the entities of a table.
     *
     * @param table
     * @return
     */
    List<VariableEntity> getVariableEntities(ValueTable table);

    /**
     * Page the entities of a table.
     *
     * @param table
     * @param offset
     * @param limit
     * @return
     */
    List<VariableEntity> getVariableEntities(ValueTable table, int offset, int limit);

    /**
     * Get the count of the variable entities.
     *
     * @param table
     * @return
     */
    int getVariableEntityCount(ValueTable table);

    /**
     * Check whether there is an entity for a table.
     *
     * @param table
     * @param entity
     * @return
     */
    boolean hasVariableEntity(ValueTable table, VariableEntity entity);

    /**
     * Delete all variable entities of a table.
     *
     * @param table
     */
    void delete(ValueTable table);

  }
}

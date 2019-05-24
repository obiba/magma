package org.obiba.magma.support;

import org.obiba.magma.VariableEntity;

import java.util.List;

/**
 * A provider that does not store the entities itself, but rather delegates to the data store backend.
 */
public interface PagingVariableEntityProvider extends VariableEntityProvider {

  /**
   * Get a sub list of the entities.
   *
   * @param offset
   * @param limit
   * @return
   */
  List<VariableEntity> getVariableEntities(int offset, int limit);

  /**
   * Check a entity exists in the data store.
   *
   * @param entity
   * @return
   */
  boolean hasVariableEntity(VariableEntity entity);
}

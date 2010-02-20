package org.obiba.magma.datasource.hibernate.domain;

import java.util.Date;

/**
 * Implemented by some persisted entities to provide access to the update timestamp and the creation timestamp.
 */
public interface Timestamp {

  /**
   * Returns the timestamp for the creation this entity.
   * @return The creation timestamp.
   */
  public Date getCreated();

  /**
   * Returns the timestamp for the last update of this entity.
   * @return The update timestamp.
   */
  public Date getUpdated();

}

package org.obiba.magma.support;

import org.obiba.magma.ValueTable;

public interface ValueTableWrapper extends ValueTable {

  /**
   * Return wrapped table
   *
   * @return
   */
  ValueTable getWrappedValueTable();

  /**
   * Return the first wrapped table
   *
   * @return
   */
  ValueTable getInnermostWrappedValueTable();

}

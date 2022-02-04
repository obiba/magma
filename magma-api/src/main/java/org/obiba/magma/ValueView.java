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

import org.obiba.magma.views.ViewAwareDatasource;

import javax.annotation.Nullable;

/**
 * Represent a logical/indirect dataset and gives access to the variables and the entities. This table can be attached
 * to any {@link org.obiba.magma.views.ViewAwareDatasource}.
 */
public interface ValueView extends ValueTable {

  /**
   * Name of the view.
   */
  void setName(String name);

  /**
   * Parent datasource to which the view is attached.
   */
  void setDatasource(ViewAwareDatasource datasource);

  /**
   * Check it is a logical table.
   *
   * @return
   */
  default boolean isView() {
    return true;
  }

  /**
   * A variable writer, to handle view's variables changes.
   *
   * @return
   */
  ValueTableWriter.VariableWriter createVariableWriter();

  /**
   * Set created timestamp.
   *
   * @param created
   */
  void setCreated(@Nullable Value created);

  /**
   * Set last update timestamp.
   *
   * @param updated
   */
  void setUpdated(@Nullable Value updated);

}

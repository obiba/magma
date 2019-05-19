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

import javax.validation.constraints.NotNull;

/**
 * Listener to events on ValueTable
 */
public interface ValueTableUpdateListener {

  /**
   * Called when a value table is being renamed.
   *
   * @param newName
   */
  void onRename(@NotNull ValueTable vt, String newName);

  /**
   * Called when a variable is being renamed.
   *
   * @param vt
   * @param v
   * @param newName
   */
  void onRename(@NotNull ValueTable vt, Variable v, String newName);

  /**
   * Called when a value table is deleted.
   *
   * @param vt
   */
  void onDelete(@NotNull ValueTable vt);

  /**
   * Called when a variable is deleted.
   *
   * @param vt
   * @param v
   */
  void onDelete(@NotNull ValueTable vt, Variable v);


}

/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.magma;

import javax.annotation.Nonnull;

/**
 * Listener to events on ValueTable
 */
public interface ValueTableUpdateListener {

  /**
   * Called a value table is being renamed.
   * @param newName
   */
  void onRename(@Nonnull ValueTable vt, String newName);

  /**
   * Called when a value table is deleted.
   * @param vt
   */
  void onDelete(@Nonnull ValueTable vt);

}

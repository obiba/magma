/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.views.support.VariableOperationContext;

import javax.annotation.Nullable;

public interface ViewPersistenceStrategy extends Initialisable, Disposable {

  /**
   * Add or update the set of views for the datasource.
   *
   * @param datasourceName
   * @param views
   */
  void writeViews(@NotNull String datasourceName, @NotNull Set<View> views, @Nullable String comment,
      @Nullable VariableOperationContext context);

  /**
   * Add or update the view for the datasource.
   *
   * @param datasourceName
   * @param view
   */
  void writeView(@NotNull String datasourceName, @NotNull View view, @Nullable String comment, @Nullable
      VariableOperationContext context);

  /**
   * Remove a specified view of a datasource. Silently ignore if no such view exists.
   *
   * @param datasourceName
   * @param viewName
   */
  void removeView(@NotNull String datasourceName, @NotNull String viewName);

  /**
   * Remove all views of a datasource.
   *
   * @param datasourceName
   */
  void removeViews(String datasourceName);

  /**
   * Read all views of the datasource.
   *
   * @param datasourceName
   * @return
   */
  Set<View> readViews(@NotNull String datasourceName);

}

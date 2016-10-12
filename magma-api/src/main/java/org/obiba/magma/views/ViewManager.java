/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
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

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;
import org.obiba.magma.views.support.VariableOperationContext;

import edu.umd.cs.findbugs.annotations.Nullable;

@SuppressWarnings("UnusedDeclaration")
public interface ViewManager extends Decorator<Datasource> {

  void addView(@NotNull String datasourceName, @NotNull View view, @Nullable String comment, @Nullable
      VariableOperationContext context);

  void removeView(@NotNull String datasourceName, @NotNull String viewName);

  void removeAllViews(@NotNull String datasourceName);

  void unregisterDatasource(@NotNull String datasourceName);

  boolean hasView(@NotNull String datasourceName, @NotNull String viewName);

  View getView(@NotNull String datasourceName, @NotNull String viewName);

  void addViews(@NotNull String datasource, Set<View> views, @Nullable String comment);
}

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

import org.obiba.magma.ValueView;
import org.obiba.magma.views.support.VariableOperationContext;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Simple Map storage. Not for production!
 */
public class MemoryViewPersistenceStrategy implements ViewPersistenceStrategy {

  private final Map<String, Map<String, ValueView>> map = new HashMap<>();

  @Override
  public void initialise() {
  }

  @Override
  public void dispose() {
    map.clear();
  }

  @Override
  public void writeViews(@NotNull String datasourceName, @NotNull Set<ValueView> views, @Nullable String comment, @Nullable
      VariableOperationContext context) {
    Map<String, ValueView> datasourceViews = getSafeDatasourceViews(datasourceName);
    for (ValueView view : views) {
      datasourceViews.put(view.getName(), view);
    }
  }

  @Override
  public void writeView(@NotNull String datasourceName, @NotNull ValueView view, @Nullable String comment, @Nullable
      VariableOperationContext context) {
    Map<String, ValueView> datasourceViews = getSafeDatasourceViews(datasourceName);
    datasourceViews.put(view.getName(), view);
  }

  @NotNull
  private Map<String, ValueView> getSafeDatasourceViews(String datasourceName) {
    Map<String, ValueView> datasourceViews = map.get(datasourceName);
    if (datasourceViews == null) {
      datasourceViews = new HashMap<>();
      map.put(datasourceName, datasourceViews);
    }
    return datasourceViews;
  }

  @Override
  public void removeView(@NotNull String datasourceName, @NotNull String viewName) {
    Map<String, ValueView> datasourceViews = map.get(datasourceName);
    if (datasourceViews != null) {
      datasourceViews.remove(viewName);
    }
  }

  @Override
  public void removeViews(String datasourceName) {
    map.remove(datasourceName);
  }

  @Override
  public Set<ValueView> readViews(@NotNull String datasourceName) {
    Map<String, ValueView> datasourceViews = map.get(datasourceName);
    return datasourceViews == null ? Collections.<ValueView>emptySet() : new HashSet<>(datasourceViews.values());
  }

}

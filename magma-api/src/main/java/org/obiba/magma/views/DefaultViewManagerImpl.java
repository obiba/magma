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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.obiba.magma.*;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.support.VariableOperationContext;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefaultViewManagerImpl implements ViewManager, Initialisable, Disposable {

  @NotNull
  private final Set<ViewAwareDatasource> viewAwareDatasources = Collections
      .synchronizedSet(new HashSet<ViewAwareDatasource>());

  @NotNull
  private final ViewPersistenceStrategy viewPersistenceStrategy;

  public DefaultViewManagerImpl(@NotNull ViewPersistenceStrategy viewPersistenceStrategy) {
    this.viewPersistenceStrategy = viewPersistenceStrategy;
  }

  @Override
  public Datasource decorate(@NotNull Datasource datasource) {
    Set<ValueView> views = viewPersistenceStrategy.readViews(datasource.getName());
    ViewAwareDatasource viewAwareDatasource = new ViewAwareDatasource(datasource, views);
    // remove an older viewAware datasource with the same name and possibly different types
    unregisterDatasource(datasource.getName());
    viewAwareDatasources.add(viewAwareDatasource);

    return viewAwareDatasource;
  }

  @Override
  public void release(Datasource datasource) {
    if (datasource == null) return;
    ViewAwareDatasource viewAwareDatasource = null;
    for (ViewAwareDatasource ds : viewAwareDatasources) {
      if (ds.getName().equals(datasource.getName())) {
        viewAwareDatasource = ds;
        break;
      }
    }
    if (viewAwareDatasource != null) {
      viewAwareDatasources.remove(viewAwareDatasource);
    }
  }

  @Override
  public void addView(@NotNull String datasourceName, @NotNull ValueView view, @Nullable String comment,
                      @Nullable VariableOperationContext context) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    //noinspection ConstantConditions
    Preconditions.checkArgument(view != null, "view cannot be null.");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(view.getName()), "view name cannot be null or empty.");

    ViewAwareDatasource datasource = getViewAwareDatasource(datasourceName);

    // Check that variables have the same entity type as the from table
    if (view instanceof Initialisable) ((Initialisable) view).initialise();
    validateVariablesEntityType(view);
    datasource.addView(view);
    try {
      viewPersistenceStrategy.writeView(datasource.getName(), view, comment, context);
    } catch (RuntimeException e) {
      // rollback
      datasource.removeView(view.getName());
      throw e;
    }
  }

  private void validateVariablesEntityType(ValueTable view) {
    for (Variable variable : view.getVariables()) {
      if (!view.getEntityType().equals(variable.getEntityType())) {
        throw new IncompatibleEntityTypeException(variable.getName(), view.getEntityType(), variable.getEntityType());
      }
    }
  }

  @Override
  public void removeView(@NotNull String datasourceName, @NotNull String viewName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(viewName), "viewName cannot be null or empty.");

    ViewAwareDatasource datasource = getViewAwareDatasource(datasourceName);
    ValueView view = datasource.getView(viewName);
    datasource.removeView(viewName);
    try {
      viewPersistenceStrategy.removeView(datasource.getName(), viewName);
    } catch (RuntimeException e) {
      // rollback
      datasource.addView(view);
      throw e;
    }
  }

  @Override
  public void removeAllViews(@NotNull String datasourceName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    viewPersistenceStrategy.removeViews(datasourceName);
  }

  @Override
  public void initView(String datasourceName, String viewName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(viewName), "viewName cannot be null or empty.");

    ViewAwareDatasource datasource = getViewAwareDatasource(datasourceName);
    ValueView view = datasource.getView(viewName);
    Disposables.silentlyDispose(view);
    Initialisables.initialise(view);
  }

  @Override
  public void unregisterDatasource(@NotNull String datasourceName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    try {
      ViewAwareDatasource viewAwareDatasource = getViewAwareDatasource(datasourceName);
      viewAwareDatasources.remove(viewAwareDatasource);
    } catch (NoSuchDatasourceException e) {
    }
  }

  @Override
  public boolean hasView(@NotNull String datasourceName, @NotNull String viewName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(viewName), "viewName cannot be null or empty.");
    return getViewAwareDatasource(datasourceName).hasView(viewName);
  }

  /**
   * Return the {@link View} from the datasource with the supplied viewName.
   *
   * @param datasourceName
   * @param viewName
   * @return The View retrieved from the specified datasource and view.
   * @throws MagmaRuntimeException     if the parameters are null or empty
   * @throws NoSuchDatasourceException if a Datasource with the supplied datasourceName does not exist
   * @throws NoSuchValueTableException if a View with the supplied viewName does not exist in datasourceName
   */
  @Override
  public ValueView getView(@NotNull String datasourceName, @NotNull String viewName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(viewName), "viewName cannot be null or empty.");
    return getViewAwareDatasource(datasourceName).getView(viewName);
  }

  @Override
  public void addViews(@NotNull String datasource, Set<ValueView> views, @Nullable String comment) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasource), "datasource cannot be null or empty.");
    viewPersistenceStrategy.writeViews(datasource, views, comment, null);
  }

  @NotNull
  private ViewAwareDatasource getViewAwareDatasource(@NotNull String datasourceName) throws NoSuchDatasourceException {
    for (ViewAwareDatasource datasource : viewAwareDatasources) {
      if (datasource.getName().equals(datasourceName)) {
        return datasource;
      }
    }
    throw new NoSuchDatasourceException(datasourceName);
  }

  @Override
  public void initialise() {
    viewPersistenceStrategy.initialise();
  }

  @Override
  public void dispose() {
    viewPersistenceStrategy.dispose();
  }

}

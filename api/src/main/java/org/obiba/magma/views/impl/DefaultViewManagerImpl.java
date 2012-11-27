package org.obiba.magma.views.impl;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewAwareDatasource;
import org.obiba.magma.views.ViewManager;
import org.obiba.magma.views.ViewPersistenceStrategy;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@SuppressWarnings("UnusedDeclaration")
public class DefaultViewManagerImpl implements ViewManager, Initialisable, Disposable {

  private final Set<ViewAwareDatasource> viewAwareDatasources = Sets.newHashSet();

  private final ViewPersistenceStrategy viewPersistenceStrategy;

  public DefaultViewManagerImpl(ViewPersistenceStrategy viewPersistenceStrategy) {
    this.viewPersistenceStrategy = viewPersistenceStrategy;
  }

  @Override
  public Datasource decorate(Datasource datasource) {
    Set<View> views = viewPersistenceStrategy.readViews(datasource.getName());
    ViewAwareDatasource viewAwareDatasource = new ViewAwareDatasource(datasource, views);

    // register the viewAware and make sure there is only one with the datasource name...
    if(getViewAwareFromName(datasource.getName()) == null) {
      viewAwareDatasources.add(viewAwareDatasource);
    }
    return viewAwareDatasource;
  }

  @Override
  public void addView(String datasourceName, View view) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    Preconditions.checkArgument(view != null, "view cannot be null.");

    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);

    viewAwareDatasource.addView(view);
    try {
      viewPersistenceStrategy.writeViews(viewAwareDatasource.getName(), viewAwareDatasource.getViews());
    } catch(RuntimeException e) {
      // rollback
      viewAwareDatasource.removeView(view.getName());
      throw e;
    }
  }

  @Override
  public void removeView(String datasourceName, String viewName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");

    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);

    Preconditions.checkArgument(!Strings.isNullOrEmpty(viewName), "viewName cannot be null or empty.");

    View view = viewAwareDatasource.getView(viewName);
    viewAwareDatasource.removeView(viewName);
    try {
      viewPersistenceStrategy.writeViews(viewAwareDatasource.getName(), viewAwareDatasource.getViews());
    } catch(RuntimeException e) {
      // rollback
      viewAwareDatasource.addView(view);
      throw e;
    }
  }

  @Override
  public void removeAllViews(String datasourceName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    viewPersistenceStrategy.writeViews(datasourceName, ImmutableSet.<View>of());
  }

  @Override
  public boolean hasView(String datasourceName, String viewName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(viewName), "viewName cannot be null or empty.");

    return viewAwareDatasource.hasView(viewName);
  }

  /**
   * Return the {@link View} from the datasource with the supplied viewName.
   *
   * @param datasourceName
   * @param viewName
   * @return The View retrieved from the specified datasource and view.
   * @throws MagmaRuntimeException if the parameters are null or empty
   * @throws NoSuchDatasourceException if a Datasource with the supplied datasourceName does not exist
   * @throws NoSuchValueTableException if a View with the supplied viewName does not exist in datasourceName
   */
  @Override
  public View getView(String datasourceName, String viewName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(viewName), "viewName cannot be null or empty.");

    return viewAwareDatasource.getView(viewName);
  }

  @Override
  public void addViews(String datasource, Set<View> views) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasource), "datasource cannot be null or empty.");
    viewPersistenceStrategy.writeViews(datasource, views);
  }

  private ViewAwareDatasource getViewAwareFromName(String datasourceName) {
    for(ViewAwareDatasource viewAwareDatasource : viewAwareDatasources) {
      if(viewAwareDatasource.getName().equals(datasourceName)) return viewAwareDatasource;
    }
    return null;
  }

  @Override
  public void initialise() {
    // Initialise persistence strategy. Nothing required.
  }

  @Override
  public void dispose() {
    // Dispose of persistence strategy. Nothing required.
  }

}

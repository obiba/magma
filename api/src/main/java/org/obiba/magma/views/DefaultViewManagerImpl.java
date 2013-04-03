package org.obiba.magma.views;

import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.Disposable;
import org.obiba.magma.IncompatibleEntityTypeException;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Variable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@SuppressWarnings("UnusedDeclaration")
public class DefaultViewManagerImpl implements ViewManager, Initialisable, Disposable {

  @Nonnull
  private final Set<ViewAwareDatasource> viewAwareDatasources = Sets.newHashSet();

  @Nonnull
  private final ViewPersistenceStrategy viewPersistenceStrategy;

  public DefaultViewManagerImpl(@Nonnull ViewPersistenceStrategy viewPersistenceStrategy) {
    this.viewPersistenceStrategy = viewPersistenceStrategy;
  }

  @Override
  public Datasource decorate(@Nonnull Datasource datasource) {
    Set<View> views = viewPersistenceStrategy.readViews(datasource.getName());
    ViewAwareDatasource viewAwareDatasource = new ViewAwareDatasource(datasource, views);

    // register the viewAware and make sure there is only one with the datasource name...
    if(getViewAwareFromName(datasource.getName()) == null) {
      viewAwareDatasources.add(viewAwareDatasource);
    }
    return viewAwareDatasource;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void addView(@Nonnull String datasourceName, @Nonnull View view) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    Preconditions.checkArgument(view != null, "view cannot be null.");

    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);

    Preconditions.checkArgument(!Strings.isNullOrEmpty(view.getName()), "view name cannot be null or empty.");

    // Check that variables have the same entity type as the from table
    view.initialise();
    for(Variable v : view.getVariables()) {
      if(!view.getEntityType().equals(v.getEntityType())) {
        throw new IncompatibleEntityTypeException(view.getEntityType(), v.getEntityType());
      }
    }

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
  public void removeView(@Nonnull String datasourceName, @Nonnull String viewName) {
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
  public void removeAllViews(@Nonnull String datasourceName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    viewPersistenceStrategy.writeViews(datasourceName, ImmutableSet.<View>of());
  }

  @Override
  public boolean hasView(@Nonnull String datasourceName, @Nonnull String viewName) {
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
  public View getView(@Nonnull String datasourceName, @Nonnull String viewName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasourceName), "datasourceName cannot be null or empty.");
    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(viewName), "viewName cannot be null or empty.");

    return viewAwareDatasource.getView(viewName);
  }

  @Override
  public void addViews(@Nonnull String datasource, Set<View> views) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(datasource), "datasource cannot be null or empty.");
    viewPersistenceStrategy.writeViews(datasource, views);
  }

  private ViewAwareDatasource getViewAwareFromName(@Nonnull String datasourceName) {
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

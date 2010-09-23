package org.obiba.magma.views;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceTransformer;
import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;

import com.google.common.collect.Sets;

public class ViewManager implements DatasourceTransformer, Initialisable, Disposable {

  private Set<ViewAwareDatasource> viewAwareDatasources = Sets.<ViewAwareDatasource> newHashSet();

  private final ViewPersistenceStrategy viewPersistenceStrategy;

  public ViewManager(ViewPersistenceStrategy viewPersistenceStrategy) {
    this.viewPersistenceStrategy = viewPersistenceStrategy;
  }

  @Override
  public Datasource transform(Datasource datasource) {
    Set<View> views = viewPersistenceStrategy.readViews(datasource.getName());
    Set<ValueTable> valueTables = Sets.<ValueTable> newHashSet(views);
    ViewAwareDatasource viewAwareDatasource = new ViewAwareDatasource(datasource, valueTables);

    // register the viewAware and make sure there is only one with the datasource name...
    if(getViewAwareFromName(datasource.getName()) == null) {
      viewAwareDatasources.add(viewAwareDatasource);
    }
    return viewAwareDatasource;
  }

  public void addView(String datasourceName, View view) {
    if(datasourceName == null || datasourceName.equals("")) throw new MagmaRuntimeException("datasourceName cannot be null or empty.");
    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);
    if(view == null) throw new MagmaRuntimeException("view cannot be null.");

    viewAwareDatasource.addView(view);
    viewPersistenceStrategy.writeViews(viewAwareDatasource.getName(), viewAwareDatasource.getViews());
  }

  public void removeView(String datasourceName, String viewName) {
    if(datasourceName == null || datasourceName.equals("")) throw new MagmaRuntimeException("datasourceName cannot be null or empty.");
    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);
    if(viewName == null || viewName.equals("")) throw new MagmaRuntimeException("viewName cannot be null or empty.");

    viewAwareDatasource.removeView(viewName);
    viewPersistenceStrategy.writeViews(viewAwareDatasource.getName(), viewAwareDatasource.getViews());
  }

  public boolean hasView(String datasourceName, String viewName) {
    if(datasourceName == null || datasourceName.equals("")) throw new MagmaRuntimeException("datasourceName cannot be null or empty.");
    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);
    if(viewName == null || viewName.equals("")) throw new MagmaRuntimeException("viewName cannot be null or empty.");

    return viewAwareDatasource.hasView(viewName);
  }

  /**
   * Return the {@link View} from the datasource with the supplied viewName.
   * @param datasourceName
   * @param viewName
   * @return The View retrieved from the specified datasource and view.
   * @throws MagmaRuntimeException if the parameters are null or empty
   * @throws NoSuchDatasourceException if a Datasource with the supplied datasourceName does not exist
   * @throws NoSuchValueTableException if a View with the supplied viewName does not exist in datasourceName
   */
  public View getView(String datasourceName, String viewName) {
    if(datasourceName == null || datasourceName.equals("")) throw new MagmaRuntimeException("datasourceName cannot be null or empty.");
    ViewAwareDatasource viewAwareDatasource = getViewAwareFromName(datasourceName);
    if(viewAwareDatasource == null) throw new NoSuchDatasourceException(datasourceName);
    if(viewName == null || viewName.equals("")) throw new MagmaRuntimeException("viewName cannot be null or empty.");

    return viewAwareDatasource.getView(viewName);
  }

  public void addViews(String datasource, Set<View> views) {
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

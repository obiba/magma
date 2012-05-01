package org.obiba.magma.views;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.DateTimeType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ViewAwareDatasource extends AbstractDatasourceWrapper {

  private final Set<View> views;

  public ViewAwareDatasource(Datasource datasource, Set<View> views) {
    super(datasource);
    if(views == null) throw new IllegalArgumentException("views cannot be null");

    this.views = Sets.newHashSet(views);
  }

  public void initialise() {
    super.initialise();

    // Initialise the views.
    for(View view : views) {
      view.setDatasource(this);
      Initialisables.initialise(view);
    }
  }

  public void dispose() {
    Disposables.dispose(getWrappedDatasource(), views);
  }

  public ValueTableWriter createWriter(String tableName, String entityType) {
    if(hasView(tableName)) {
      throw new UnsupportedOperationException("Cannot write to a View");
    }
    return getWrappedDatasource().createWriter(tableName, entityType);
  }

  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return hasView(name) ? this.getView(name) : this.getWrappedDatasource().getValueTable(name);
  }

  public Set<ValueTable> getValueTables() {
    return Sets.union(getWrappedTables(), views);
  }

  @Override
  public boolean hasValueTable(String name) {
    return hasView(name) || getWrappedDatasource().hasValueTable(name);
  }

  @Override
  public boolean canDropTable(String name) {
    if(hasView(name)) return true;
    return getWrappedDatasource().canDropTable(name);
  }

  @Override
  public void dropTable(String name) {
    if(hasView(name)) {
      removeView(name);
    } else {
      getWrappedDatasource().dropTable(name);
    }
  }

  public Set<View> getViews() {
    return ImmutableSet.copyOf(views);
  }

  /**
   * Add or replace View.
   */
  public void addView(View view) {
    if(getWrappedDatasource().hasValueTable(view.getName())) {
      throw new IllegalArgumentException("can't add view to datasource: a table with this name '" + view.getName() + "' already exists");
    }

    if(hasView(view.getName())) {
      view.setCreated(getView(view.getName()).getTimestamps().getCreated());
      view.setUpdated(DateTimeType.get().now());
      removeView(view.getName());
    }

    Initialisables.initialise(view);
    views.add(view);
    view.setDatasource(this);
  }

  public void removeView(String name) {
    View view = getViewByName(name);
    if(view != null) {
      views.remove(view);
      Disposables.dispose(view);
    }
  }

  public boolean hasView(String name) {
    return getViewByName(name) != null;
  }

  public View getView(String name) {
    View view = getViewByName(name);
    if(view != null) {
      return view;
    }
    throw new NoSuchValueTableException(getName(), name);
  }

  private View getViewByName(String name) {
    for(View view : views) {
      if(view.getName().equals(name)) {
        return view;
      }
    }
    return null;
  }

  private Set<ValueTable> getWrappedTables() {
    return getWrappedDatasource().getValueTables();
  }
}

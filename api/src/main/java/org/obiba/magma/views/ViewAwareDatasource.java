package org.obiba.magma.views;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.DateTimeType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ViewAwareDatasource extends AbstractDatasourceWrapper {

  private final Map<String, View> views;

  public ViewAwareDatasource(Datasource datasource, Iterable<View> views) {
    super(datasource);
    if(views == null) throw new IllegalArgumentException("views cannot be null");

    this.views = Maps.newLinkedHashMap();
    for (View view : views) {
      this.views.put(view.getName(), view);
    }
  }

  @Override
  public void initialise() {
    super.initialise();

    // Initialise the views.
    for(View view : views.values()) {
      view.setDatasource(this);
      Initialisables.initialise(view);
    }
  }

  @Override
  public void dispose() {
    Disposables.dispose(getWrappedDatasource(), views);
  }

  @Nonnull
  @Override
  public ValueTableWriter createWriter(@Nonnull String tableName, @Nonnull String entityType) {
    if(hasView(tableName)) {
      throw new UnsupportedOperationException("Cannot write to a View");
    }
    return getWrappedDatasource().createWriter(tableName, entityType);
  }

  @Override
  public ValueTable getValueTable(String name) throws NoSuchValueTableException {
    return hasView(name) ? getView(name) : getWrappedDatasource().getValueTable(name);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    return Sets.union(getWrappedTables(), Sets.newHashSet(views.values()));
  }

  @Override
  public boolean hasValueTable(String name) {
    return hasView(name) || getWrappedDatasource().hasValueTable(name);
  }

  @Override
  public boolean canDropTable(String name) {
    return hasView(name) || getWrappedDatasource().canDropTable(name);
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
    return ImmutableSet.copyOf(views.values());
  }

  /**
   * Add or replace View.
   */
  public synchronized void addView(View view) {
    if(getWrappedDatasource().hasValueTable(view.getName())) {
      throw new IllegalArgumentException(
          "can't add view to datasource: a table with this name '" + view.getName() + "' already exists");
    }

    if(hasView(view.getName())) {
      view.setCreated(getView(view.getName()).getTimestamps().getCreated());
      view.setUpdated(DateTimeType.get().now());
      removeView(view.getName());
    }

    Initialisables.initialise(view);
    views.put(view.getName(), view);
    view.setDatasource(this);
  }

  public synchronized void removeView(String name) {
    if(views.containsKey(name)) {
      View view = views.get(name);
      views.remove(name);
      Disposables.dispose(view);
    }
  }

  public boolean hasView(String name) {
    return views.get(name) != null;
  }

  public View getView(String name) {
    View view = views.get(name);
    if(view != null) {
      return view;
    }
    throw new NoSuchValueTableException(getName(), name);
  }

  private Set<ValueTable> getWrappedTables() {
    return getWrappedDatasource().getValueTables();
  }
}

package org.obiba.magma.views;

import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.type.DateTimeType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ViewAwareDatasource extends AbstractDatasourceWrapper {

  private final Map<String, View> views;

  private Value lastUpdate = DateTimeType.get().nullValue();

  public ViewAwareDatasource(Datasource datasource, Iterable<View> views) {
    super(datasource);
    if(views == null) throw new IllegalArgumentException("views cannot be null");

    this.views = Maps.newLinkedHashMap();
    for(View view : views) {
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

  @NotNull
  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
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

  @Override
  public boolean canRenameTable(String name) {
    return hasView(name) || getWrappedDatasource().canRenameTable(name);
  }

  @Override
  public void renameTable(String name, String newName) {
    if(hasView(name)) {
      renameView(name, newName);
    } else {
      getWrappedDatasource().renameTable(name, newName);
    }
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    final Timestamps ts = super.getTimestamps();
    ImmutableSet.Builder<Timestamped> builder = ImmutableSet.builder();
    builder.addAll(getViews()) //
        .add(new Timestamped() {
          @NotNull
          @Override
          public Timestamps getTimestamps() {
            return ts;
          }
        }) //
        .add(new ViewAwareDatasourceTimestamped());
    return new UnionTimestamps(builder.build());
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
    lastUpdate = DateTimeType.get().now();
  }

  public synchronized void removeView(String name) {
    if(views.containsKey(name)) {
      View view = views.get(name);
      views.remove(name);
      Disposables.dispose(view);
      lastUpdate = DateTimeType.get().now();
    }
  }

  public synchronized void renameView(String name, String newName) {
    if(views.containsKey(name)) {
      View view = views.remove(name);
      view.setName(newName);
      views.put(newName, view);
      lastUpdate = DateTimeType.get().now();
    }
  }

  public boolean hasView(String name) {
    return views.get(name) != null;
  }

  @NotNull
  public View getView(String name) throws NoSuchValueTableException {
    View view = views.get(name);
    if(view != null) {
      return view;
    }
    throw new NoSuchValueTableException(getName(), name);
  }

  private Set<ValueTable> getWrappedTables() {
    return getWrappedDatasource().getValueTables();
  }

  private class ViewAwareDatasourceTimestamped implements Timestamped {
    @NotNull
    @Override
    public Timestamps getTimestamps() {
      return new Timestamps() {
        @NotNull
        @Override
        public Value getLastUpdate() {
          return lastUpdate;
        }

        @NotNull
        @Override
        public Value getCreated() {
          return DateTimeType.get().nullValue();
        }
      };
    }
  }
}

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

import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.*;
import org.obiba.magma.support.AbstractDatasourceWrapper;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.magma.type.DateTimeType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ViewAwareDatasource extends AbstractDatasourceWrapper {

  private final Map<String, ValueView> views;

  private Value lastUpdate = DateTimeType.get().nullValue();

  public ViewAwareDatasource(Datasource datasource, Iterable<ValueView> views) {
    super(datasource);
    if(views == null) throw new IllegalArgumentException("views cannot be null");

    this.views = Maps.newLinkedHashMap();
    for(ValueView view : views) {
      this.views.put(view.getName(), view);
    }
  }

  @Override
  public void initialise() {
    super.initialise();

    // Initialise the views.
    for(ValueView view : views.values()) {
      view.setDatasource(this);
      Initialisables.silentlyInitialise(view);
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

  public Set<ValueView> getViews() {
    return ImmutableSet.copyOf(views.values());
  }

  /**
   * Add or replace View.
   */
  public synchronized void addView(ValueView view) {
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
      ValueView view = views.get(name);
      evictVariableEntitiesCache(view);
      views.remove(name);
      Disposables.dispose(view);
      lastUpdate = DateTimeType.get().now();
    }
  }

  public synchronized void renameView(String name, String newName) {
    if(views.containsKey(name)) {
      ValueView view = views.remove(name);
      evictVariableEntitiesCache(view);
      view.setName(newName);
      views.put(newName, view);
      lastUpdate = DateTimeType.get().now();
    }
  }

  private void evictVariableEntitiesCache(ValueTable view) {
    if (MagmaEngine.get().hasExtension(MagmaCacheExtension.class)) {
      MagmaCacheExtension cacheExtension = MagmaEngine.get().getExtension(MagmaCacheExtension.class);
      if (cacheExtension.hasVariableEntitiesCache()) {
        cacheExtension.getVariableEntitiesCache().evict(view.getTableReference());
      }
    }
  }

  public boolean hasView(String name) {
    return views.get(name) != null;
  }

  @NotNull
  public ValueView getView(String name) throws NoSuchValueTableException {
    ValueView view = views.get(name);
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

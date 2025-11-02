/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.springframework.cache.CacheManager;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CachedDatasource extends AbstractDatasourceWrapper {

  private final CacheManager cacheManager;

  private final Map<String, ValueTable> cachedValueTablesMap = Maps.newHashMap();

  public CachedDatasource(@NotNull Datasource wrapped, @NotNull CacheManager cacheManager) {
    super(wrapped);
    this.cacheManager = cacheManager;
  }

  @Override
  public Datasource getWrappedDatasource() {
    Datasource wrapped = super.getWrappedDatasource();

    if (wrapped == null) throw new MagmaRuntimeException("wrapped value not initialized");

    return wrapped;
  }

  @Override
  public void initialise() {
    try {
      cachedValueTablesMap.clear();
      getWrappedDatasource().initialise();
    } catch(MagmaRuntimeException ex) {
      //ignore
    }
  }

  @Override
  public boolean hasValueTable(final String tableName) {
    return getWrappedDatasource().hasValueTable(tableName);
  }

  @Override
  public ValueTable getValueTable(final String tableName) throws NoSuchValueTableException {
    if (cachedValueTablesMap.isEmpty()) getValueTables();

    if (!cachedValueTablesMap.containsKey(tableName)) throw new NoSuchValueTableException(getName(), tableName);

    return cachedValueTablesMap.get(tableName);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    List<String> tableNames = Lists.newArrayList();
    for (ValueTable table : getWrappedDatasource().getValueTables()) {
      tableNames.add(table.getName());
      if (!cachedValueTablesMap.containsKey(table.getName()))
        cachedValueTablesMap.put(table.getName(),
            new CachedValueTable(this, table.getName(),
                cacheManager.getCache(String.format("%s.%s", getName(), table.getName()))));
    }
    // remove tables that are no more in the wrapped
    for (String tableName : cachedValueTablesMap.keySet()) {
      if (!tableNames.contains(tableName)) cachedValueTablesMap.remove(tableName);
    }
    return ImmutableSet.<ValueTable>builder().addAll(cachedValueTablesMap.values()).build();
  }

  @Override
  public ValueTableWriter createWriter(String tableName, String entityType) {
    final ValueTableWriter writer = super.createWriter(tableName, entityType);
    return new CachedValueTableWriter(writer, tableName);
  }

  @Override
  public void dropTable(String name) {
    super.dropTable(name);
    evictTableCache(name);
  }

  private void evictTableCache(String name) {
    cachedValueTablesMap.clear();
    Objects.requireNonNull(cacheManager.getCache(String.format("%s.%s", getName(), name))).clear();
  }

  private class CachedValueTableWriter implements ValueTableWriter {
    private final ValueTableWriter writer;
    private final String tableName;

    public CachedValueTableWriter(ValueTableWriter writer, String tableName) {
      this.writer = writer;
      this.tableName = tableName;
    }

    @Override
    public VariableWriter writeVariables() {
      return writer.writeVariables();
    }

    @Override
    public ValueSetWriter writeValueSet(VariableEntity entity) {
      return writer.writeValueSet(entity);
    }

    @Override
    public void close() {
      evictTableCache(tableName);
    }
  }
}

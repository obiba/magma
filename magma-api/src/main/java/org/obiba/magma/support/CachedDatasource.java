package org.obiba.magma.support;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.springframework.cache.Cache;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CachedDatasource extends AbstractDatasourceWrapper {
  private Cache cache;

  private final Map<String, ValueTable> cachedValueTablesMap = Maps.newHashMap();

  public CachedDatasource(@NotNull Datasource wrapped, @NotNull Cache cache) {
    super(wrapped);
    this.cache = cache;
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
    return getCached(getCacheKey("hasValueTable", tableName), new Supplier<Boolean>() {
      @Override
      public Boolean get() {
        return getWrappedDatasource().hasValueTable(tableName);
      }
    });
  }

  @Override
  public ValueTable getValueTable(final String tableName) throws NoSuchValueTableException {
    if (cachedValueTablesMap.isEmpty()) getValueTables();

    if (!cachedValueTablesMap.containsKey(tableName)) throw new NoSuchValueTableException(getName(), tableName);

    return cachedValueTablesMap.get(tableName);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    if (!cachedValueTablesMap.isEmpty()) return ImmutableSet.<ValueTable>builder().addAll(cachedValueTablesMap.values()).build();

    List<String> valueTableNames = getCached(getCacheKey("getValueTables"), new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        List<String> res = Lists.newArrayList();

        for(ValueTable table : getWrappedDatasource().getValueTables()) {
          res.add(table.getName());
        }

        return res;
      }
    });

    for(String tableName : valueTableNames) {
      cachedValueTablesMap.put(tableName, new CachedValueTable(this, tableName, cache));
    }

    return ImmutableSet.<ValueTable>builder().addAll(cachedValueTablesMap.values()).build();
  }

  public void evictValues(VariableEntity variableEntity) {
    for (ValueTable valueTable: getValueTables()) {
      ((CachedValueTable)valueTable).evictValues(variableEntity);
    }
  }

  private <T> T getCached(Object key, Supplier<T> supplier) {
    return CacheUtils.getCached(cache, key, supplier);
  }

  private String getCacheKey(Object... parts) {
    return Joiner.on(".").join(Iterables.concat(Arrays.asList(getName()), Arrays.asList(parts)));
  }
}

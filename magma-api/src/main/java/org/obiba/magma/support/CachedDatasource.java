package org.obiba.magma.support;

import java.util.Arrays;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.springframework.cache.Cache;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class CachedDatasource extends AbstractDatasourceWrapper {
  private Cache cache;

  public CachedDatasource(@NotNull Datasource wrapped, @NotNull Cache cache) {
    super(wrapped);
    this.cache = cache;
  }

  @Override
  public void initialise() {
    try {
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
    return new CachedValueTable(this, getWrappedDatasource().getValueTable(tableName), cache);
  }

  @Override
  public Set<ValueTable> getValueTables() {
    Set<ValueTable> res = Sets.newHashSet();

    for(ValueTable table : getWrappedDatasource().getValueTables()) {
      res.add(new CachedValueTable(this, table, cache));
    }

    return res;
  }

  private <T> T getCached(Object key, Supplier<T> supplier) {
    return CacheUtils.getCached(cache, key, supplier);
  }

  private String getCacheKey(Object... parts) {
    return Joiner.on(".").join(Iterables.concat(Arrays.asList(getName()), Arrays.asList(parts)));
  }
}

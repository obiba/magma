package org.obiba.magma.support;

import java.util.Arrays;
import java.util.List;
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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CachedDatasource extends AbstractDatasourceWrapper {
  private Cache cache;

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
    return new CachedValueTable(this, tableName, cache);
  }

  @Override
  public Set<ValueTable> getValueTables() {
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

    Set<ValueTable> res = Sets.newHashSet();

    for(String tableName : valueTableNames) {
      res.add(new CachedValueTable(this, tableName, cache));
    }

    return res;
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

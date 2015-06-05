package org.obiba.magma.support;

import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.springframework.cache.Cache;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

public class CachedTimestamps implements Timestamps {

  private ValueTable table;
  private ValueSet valueSet;
  private Cache cache;
  private Timestamps wrapped;

  public CachedTimestamps(@NotNull Timestamps wrapped, @NotNull ValueSet valueSet, @NotNull Cache cache) {
    this.valueSet = valueSet;
    this.cache = cache;
    this.wrapped = wrapped;
  }

  public CachedTimestamps(@NotNull Timestamps wrapped, @NotNull ValueTable table, @NotNull Cache cache) {
    this.table = table;
    this.cache = cache;
    this.wrapped = wrapped;
  }

  @Override
  public Value getLastUpdate() {
    return getCached(getCacheKey("getLastUpdate"), new Supplier<Value>() {
      @Override
      public Value get() {
        return getWrapped().getLastUpdate();
      }
    });
  }

  @Override
  public Value getCreated() {
    return getCached(getCacheKey("getCreated"), new Supplier<Value>() {
      @Override
      public Value get() {
        return getWrapped().getCreated();
      }
    });
  }

  public Timestamps getWrapped() {
    return wrapped;
  }

  private <T> T getCached(Object key, Supplier<T> supplier) {
    return CacheUtils.getCached(cache, key, supplier);
  }

  private String getCacheKey(Object... parts) {
    return Joiner
        .on(".").join(Iterables.concat(
            Arrays.asList(table != null ? table.getName() : null, valueSet != null ? valueSet.getValueTable().getName() + "." + valueSet.getVariableEntity().getIdentifier() : null), Arrays.asList(parts)));
  }
}

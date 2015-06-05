package org.obiba.magma.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.springframework.cache.Cache;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

public class CachedVectorSource implements VectorSource {

  private Cache cache;
  private VectorSource wrapped;
  private VariableValueSource variableValueSource;

  public CachedVectorSource(@NotNull VectorSource wrapped, @NotNull VariableValueSource variableValueSource, @NotNull Cache cache) {
    this.cache = cache;
    this.wrapped = wrapped;
    this.variableValueSource = variableValueSource;
  }

  @Override
  public ValueType getValueType() {
    return getCached(getCacheKey("getValueType"), new Supplier<ValueType>() {
      @Override
      public ValueType get() {
        return getWrapped().getValueType();
      }
    });
  }

  @Override
  public Iterable<Value> getValues(final SortedSet<VariableEntity> entities) {
    List<String> ids = new ArrayList<>();

    for(VariableEntity ve : entities) {
      ids.add(ve.getIdentifier());
    }

    return getCached(getCacheKey("getValues", Joiner.on(".").join(ids)), new Supplier<Iterable<Value>>() {
      @Override
      public Iterable<Value> get() {
        return getWrapped().getValues(entities);
      }
    });
  }

  public VectorSource getWrapped() {
    return wrapped;
  }

  private <T> T getCached(Object key, Supplier<T> supplier) {
    return CacheUtils.getCached(cache, key, supplier);
  }

  private String getCacheKey(Object... parts) {
    return Joiner
        .on(".").join(Iterables.concat(Arrays.asList(variableValueSource.getName(), "VectorSource"), Arrays.asList(parts)));
  }
}

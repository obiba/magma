package org.obiba.magma.support;

import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.springframework.cache.Cache;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

public class CachedVariableValueSource extends AbstractVariableValueSourceWrapper {

  private Cache cache;
  private ValueTable table;

  public CachedVariableValueSource(@NotNull VariableValueSource wrapped, @NotNull ValueTable table, @NotNull Cache cache) {
    super(wrapped);
    this.cache = cache;
    this.table = table;
  }

  @NotNull
  @Override
  public Variable getVariable() {
    return getCached(getCacheKey("getVariable"), new Supplier<Variable>() {
      @Override
      public Variable get() {
        return getWrapped().getVariable();
      }
    });
  }

  @NotNull
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
  @NotNull
  public Value getValue(final ValueSet valueSet) {
    return getCached(getCacheKey("getValue", valueSet.getValueTable().getName(), valueSet.getVariableEntity().getIdentifier()), new Supplier<Value>() {
      @Override
      public Value get() {
        return getWrapped().getValue(valueSet);
      }
    });
  }

  @Override
  public boolean supportVectorSource() {
    return getCached(getCacheKey("supportVectorSource"), new Supplier<Boolean>() {
      @Override
      public Boolean get() {
        return getWrapped().supportVectorSource();
      }
    });
  }

  @NotNull
  @Override
  public VectorSource asVectorSource() {
    return new CachedVectorSource(getWrapped().asVectorSource(), getWrapped(), cache);
  }

  @NotNull
  @Override
  public String getName() {
    return getWrapped().getName();
  }

  private <T> T getCached(Object key, Supplier<T> supplier) {
    return CacheUtils.getCached(cache, key, supplier);
  }

  private String getCacheKey(Object... parts) {
    return Joiner
        .on(".").join(Iterables.concat(Arrays.asList(table.getName(), getWrapped().getName()), Arrays.asList(parts)));
  }
}

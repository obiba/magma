package org.obiba.magma.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.springframework.cache.Cache;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;


public class CachedValueTable extends AbstractValueTableWrapper {

  private Cache cache;
  private ValueTable wrapped;
  private Datasource datasource;

  public CachedValueTable(@NotNull Datasource datasource, @NotNull ValueTable wrapped, @NotNull Cache cache) {
    this.datasource = datasource;
    this.wrapped = wrapped;
    this.cache = cache;
  }

  @Override
  public ValueTable getWrappedValueTable() {
    return this.wrapped;
  }

  @NotNull
  @Override
  public Datasource getDatasource() {
    return this.datasource;
  }

  @Override
  public String getEntityType() {
    return getCached(getCacheKey("getEntityType"), new Supplier<String>() {
      @Override
      public String get() {
        return getWrappedValueTable().getEntityType();
      }
    });
  }

  @Override
  public Value getValue(final Variable variable, final ValueSet valueSet) {
    return getCached(getCacheKey("getValue", variable.getName(), valueSet.getValueTable().getName(), valueSet.getVariableEntity().getIdentifier()), new Supplier<Value>() {
      @Override
      public Value get() {
        return getWrappedValueTable().getValue(variable, valueSet);
      }
    });
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return getCached(getCacheKey("getVariableEntities"), new Supplier<Set<VariableEntity>>() {
      @Override
      public Set<VariableEntity> get() {
        return getWrappedValueTable().getVariableEntities();
      }
    });
  }

  @Override
  public ValueSet getValueSet(final VariableEntity entity) throws NoSuchValueSetException {
    return new CachedValueSet(this, entity, getWrappedValueTable().getValueSet(entity), cache);
  }

  @Override
  public Timestamps getValueSetTimestamps(final VariableEntity entity) throws NoSuchValueSetException {
    return new CachedTimestamps(getWrappedValueTable().getValueSetTimestamps(entity), getValueSet(entity), cache);
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(final SortedSet<VariableEntity> entities) {
    List<String> ids = new ArrayList<>();

    for(VariableEntity ve : entities) {
      ids.add(ve.getIdentifier());
    }

    return getCached(getCacheKey("getValueSetTimestamps", Joiner.on(".").join(ids)), new Supplier<Iterable<Timestamps>>() {
      @Override
      public Iterable<Timestamps> get() {
        return getWrappedValueTable().getValueSetTimestamps(entities);
      }
    });
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    List<ValueSet> res = new ArrayList<>();

    for(ValueSet valueSet: getWrappedValueTable().getValueSets()) {
      res.add(new CachedValueSet(this, valueSet.getVariableEntity(), valueSet, cache));
    }

    return res;
  }

  @Override
  public boolean hasVariable(final String name) {
    return getCached(getCacheKey("hasVariable", name), new Supplier<Boolean>() {
      @Override
      public Boolean get() {
        return getWrappedValueTable().hasVariable(name);
      }
    });
  }

  @Override
  public Variable getVariable(final String name) throws NoSuchVariableException {
    return getCached(getCacheKey("getVariable", name), new Supplier<Variable>() {
      @Override
      public Variable get() {
        return getWrappedValueTable().getVariable(name);
      }
    });
  }

  @Override
  public Iterable<Variable> getVariables() {
    return getCached(getCacheKey("getVariables"), new Supplier<Iterable<Variable>>() {
      @Override
      public Iterable<Variable> get() {
        return getWrappedValueTable().getVariables();
      }
    });
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    return new CachedVariableValueSource(getWrappedValueTable().getVariableValueSource(variableName), this, cache);
  }

  @Override
  public boolean hasValueSet(final VariableEntity entity) {
    return getCached(getCacheKey("hasValueSet", entity.getIdentifier()), new Supplier<Boolean>() {
      @Override
      public Boolean get() {
        return getWrappedValueTable().hasValueSet(entity);
      }
    });
  }

  @Override
  public boolean isForEntityType(final String entityType) {
    return getCached(getCacheKey("isForEntityType", entityType), new Supplier<Boolean>() {
      @Override
      public Boolean get() {
        return getWrappedValueTable().isForEntityType(entityType);
      }
    });
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new CachedTimestamps(getWrappedValueTable().getTimestamps(), this, cache);
  }

  @Override
  public boolean isView() {
    return getCached(getCacheKey("isView"), new Supplier<Boolean>() {
      @Override
      public Boolean get() {
        return getWrappedValueTable().isView();
      }
    });
  }

  @Override
  public String getTableReference() {
    return getCached(getCacheKey("getTableReference"), new Supplier<String>() {
      @Override
      public String get() {
        return getWrappedValueTable().getTableReference();
      }
    });
  }

  @Override
  public int getVariableCount() {
    return getCached(getCacheKey("getVariableCount"), new Supplier<Integer>() {
      @Override
      public Integer get() {
        return getWrappedValueTable().getVariableCount();
      }
    });
  }

  @Override
  public int getValueSetCount() {
    return getCached(getCacheKey("getValueSetCount"), new Supplier<Integer>() {
      @Override
      public Integer get() {
        return getWrappedValueTable().getValueSetCount();
      }
    });
  }

  @Override
  public int getVariableEntityCount() {
    return getCached(getCacheKey("getVariableEntityCount"), new Supplier<Integer>() {
      @Override
      public Integer get() {
        return getWrappedValueTable().getVariableEntityCount();
      }
    });
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(wrapped.getName());
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof CachedValueTable)) return false;

    return Objects.equal(wrapped.getName(), ((CachedValueTable)other).getName());
  }

  private <T> T getCached(Object key, Supplier<T> supplier) {
    return CacheUtils.getCached(cache, key, supplier);
  }

  private String getCacheKey(Object... parts) {
    return Joiner.on(".").join(Iterables.concat(Arrays.asList(datasource.getName(), getName()), Arrays.asList(parts)));
  }
}

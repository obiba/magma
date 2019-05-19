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

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import org.obiba.magma.*;
import org.springframework.cache.Cache;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class CachedValueTable implements ValueTable {

  private Cache cache;
  private ValueTable wrapped;
  private CachedDatasource datasource;
  private String name;

  public CachedValueTable(@NotNull CachedDatasource datasource, @NotNull String tableName, @NotNull Cache cache) {
    this.name = tableName;
    this.datasource = datasource;
    this.cache = cache;

    try {
      wrapped = datasource.getWrappedDatasource().getValueTable(tableName);
    } catch( MagmaRuntimeException ex) {
      //ignore
    }
  }

  @Override
  public String getName() {
    return name;
  }

  public ValueTable getWrappedValueTable() {
    if (this.wrapped == null)
      throw new MagmaRuntimeException("wrapped value not initialized.");

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
    return new CachedValueSet(this, entity, cache);
  }

  @Override
  public boolean canDropValueSets() {
    return getCached(getCacheKey("canDropValueSets"), new Supplier<Boolean>() {
      @Override
      public Boolean get() {
        return getWrappedValueTable().canDropValueSets();
      }
    });
  }

  @Override
  public void dropValueSets() {
    getWrappedValueTable().canDropValueSets();
  }

  @Override
  public Timestamps getValueSetTimestamps(final VariableEntity entity) throws NoSuchValueSetException {
    return new CachedTimestamps(new CachedValueSet(this, entity, cache), cache);
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(final SortedSet<VariableEntity> entities) {
    List<String> ids = new ArrayList<>();

    for(VariableEntity variableEntity : entities) {
      ids.add(variableEntity.getIdentifier());
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
    return getValueSets(getVariableEntities());
  }

  @Override
  public Iterable<ValueSet> getValueSets(Iterable<VariableEntity> entities) {
    return StreamSupport.stream(entities.spliterator(), false) //
        .map(entity -> new CachedValueSet(CachedValueTable.this, entity, cache)) //
        .collect(Collectors.toList());
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
    return new CachedVariableValueSource(this, variableName, cache);
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
    return new CachedTimestamps(this, cache);
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
    return Objects.hashCode(name);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof CachedValueTable)) return false;

    return Objects.equal(name, ((CachedValueTable)other).getName());
  }

  public void evictValues(VariableEntity variableEntity) {
    try {
      for(Variable va : getVariables()) {
        cache.evict(getCacheKey("getValue", va.getName(), name, variableEntity.getIdentifier()));
        cache.evict(getCacheKey("hasValueSet", variableEntity.getIdentifier()));

        CachedVariableValueSource vs = (CachedVariableValueSource) getVariableValueSource(va.getName());
        vs.evictValues(variableEntity);
      }
    } catch(MagmaRuntimeException ex) {
      //ignore
    }
  }

  private <T> T getCached(Object key, Supplier<T> supplier) {
    return CacheUtils.getCached(cache, key, supplier);
  }

  private String getCacheKey(Object... parts) {
    return Joiner.on(".").join(Iterables.concat(Arrays.asList(datasource.getName(), name), Arrays.asList(parts)));
  }
}

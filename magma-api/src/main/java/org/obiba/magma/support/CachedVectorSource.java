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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VectorSource;
import org.springframework.cache.Cache;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class CachedVectorSource implements VectorSource {

  private Cache cache;
  private VectorSource wrapped;
  private CachedVariableValueSource variableValueSource;

  public CachedVectorSource(@NotNull CachedVariableValueSource variableValueSource, @NotNull Cache cache) {
    this.cache = cache;
    this.variableValueSource = variableValueSource;

    try {
      this.wrapped = variableValueSource.getWrapped().asVectorSource();
    } catch(MagmaRuntimeException ex) {
      //ignore
    }
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
    boolean missing = true;
    List<Value> res = new ArrayList<>();

    for(VariableEntity variableEntity : entities) {
      Cache.ValueWrapper valueWrapper = cache.get(getCacheKey("getValues", variableEntity.getIdentifier()));

      if (valueWrapper == null) {
        missing = false;
        break;
      }

      res.add((Value)valueWrapper.get());
    }

    if (!missing) return res;

    ArrayList<Value> values = Lists.newArrayList(getWrapped().getValues(entities));
    ArrayList<VariableEntity> variableEntities = Lists.newArrayList(entities);

    for(int i = 0; i < variableEntities.size(); i++) {
      cache.put(getCacheKey("getValues", variableEntities.get(i).getIdentifier()), values.get(i));
    }

    return values;
  }

  public VectorSource getWrapped() {
    if (wrapped == null) throw new MagmaRuntimeException("wrapped value not initialized");

    return wrapped;
  }

  public void evictValues(VariableEntity variableEntity) {
    cache.evict(getCacheKey("getValue", variableEntity.getIdentifier()));
  }

  private <T> T getCached(Object key, Supplier<T> supplier) {
    return CacheUtils.getCached(cache, key, supplier);
  }

  private String getCacheKey(Object... parts) {
    return Joiner
        .on(".").join(Iterables.concat(Arrays.asList(variableValueSource.getName(), "VectorSource"), Arrays.asList(parts)));
  }
}

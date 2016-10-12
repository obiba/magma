/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.support;

import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.springframework.cache.Cache;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

public class CachedVariableValueSource implements VariableValueSource {

  private Cache cache;
  private CachedValueTable table;
  private VariableValueSource wrapped;
  private String name;

  public CachedVariableValueSource(@NotNull CachedValueTable table, String variableName, @NotNull Cache cache) {
    this.cache = cache;
    this.table = table;
    this.name = variableName;

    try {
      wrapped = table.getWrappedValueTable().getVariableValueSource(variableName);
    } catch(MagmaRuntimeException ex) {
      //ignore
    }
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
    return new CachedVectorSource(this, cache);
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  public void evictValues(VariableEntity variableEntity) {
    cache.evict(getCacheKey("getValue", table.getName(), variableEntity.getIdentifier()));

    ((CachedVectorSource)asVectorSource()).evictValues(variableEntity);
  }

  private <T> T getCached(Object key, Supplier<T> supplier) {
    return CacheUtils.getCached(cache, key, supplier);
  }

  private String getCacheKey(Object... parts) {
    return Joiner
        .on(".").join(Iterables.concat(Arrays.asList(table.getName(), name), Arrays.asList(parts)));
  }

  public VariableValueSource getWrapped() {
    if (wrapped == null) throw new MagmaRuntimeException("wrapped value not initialized.");

    return wrapped;
  }
}

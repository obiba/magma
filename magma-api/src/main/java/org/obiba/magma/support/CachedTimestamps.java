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

import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.obiba.magma.MagmaRuntimeException;
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

  public CachedTimestamps(@NotNull CachedValueSet valueSet, @NotNull Cache cache) {
    this.valueSet = valueSet;
    this.cache = cache;
    try {
      this.wrapped = valueSet.getWrapped().getTimestamps();
    } catch(MagmaRuntimeException ex) {
      //ignore
    }
  }

  public CachedTimestamps(@NotNull CachedValueTable table, @NotNull Cache cache) {
    this.table = table;
    this.cache = cache;
    try {
      this.wrapped = table.getWrappedValueTable().getTimestamps();
    } catch( MagmaRuntimeException ex) {
      //ignore
    }
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
    if (wrapped == null) throw new MagmaRuntimeException("Wrapped value not initialized.");

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

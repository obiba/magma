/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views;

import java.util.concurrent.ConcurrentMap;

import org.obiba.magma.ValueSet;

import com.google.common.collect.MapMaker;

class CachingWhereClause implements WhereClause {

  /**
   * Cache the result in this map. Note that this cache will only work if == returns true for the keys. This should be
   * the case as we pass-around the same ValueSet instance all the time. It is important to use weak keys since we don't
   * want the cache to prevent collecting garbage.
   */
  // TODO: this should be handled in a general-level caching package.
  private final ConcurrentMap<ValueSet, Boolean> cache = new MapMaker().weakKeys().makeMap();

  private final WhereClause expensiveClause;

  CachingWhereClause(WhereClause expensiveClause) {
    this.expensiveClause = expensiveClause;
  }

  @Override
  public boolean where(ValueSet valueSet) {
    return where(valueSet, null);
  }

  @Override
  public boolean where(ValueSet valueSet, View view) {
    if(cache.containsKey(valueSet)) {
      return cache.get(valueSet);
    }
    boolean where = expensiveClause.where(valueSet);
    cache.put(valueSet, where);
    return where;
  }
}

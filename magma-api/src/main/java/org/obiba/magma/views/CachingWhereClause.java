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
    if(cache.containsKey(valueSet)) {
      return cache.get(valueSet);
    }
    boolean where = expensiveClause.where(valueSet);
    cache.put(valueSet, where);
    return where;
  }

}

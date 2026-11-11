/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.views;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.obiba.magma.MagmaCacheExtension;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaTest;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueView;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.IntegerType;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.google.common.collect.ImmutableList;

import static org.fest.assertions.api.Assertions.assertThat;

public class ViewVariableEntitiesCacheTest extends MagmaTest {

  private final AtomicInteger evaluations = new AtomicInteger();

  @Test
  public void test_entities_are_read_back_from_the_cache_by_a_new_view() throws Exception {
    StaticValueTable table = newTable(DateTimeType.get().now());
    ConcurrentMapCacheManager cacheManager = extendWithCache();

    assertThat(newView(table).getVariableEntityCount()).isEqualTo(2);
    assertThat(evaluations.get()).isEqualTo(3);
    ViewVariableEntityProvider.awaitCacheWrites();

    // as after a restart: another view instance, the same cache
    View view = newView(table);
    assertThat(view.getVariableEntityCount()).isEqualTo(2);
    assertThat(view.hasValueSet(new VariableEntityBean("Participant", "2"))).isTrue();
    assertThat(view.hasValueSet(new VariableEntityBean("Participant", "1"))).isFalse();
    assertThat(evaluations.get()).isEqualTo(3);
    assertThat(cacheManager.getCache("magma-table-entities").get(ViewVariableEntityProvider.getTableCacheKey(view)))
        .isNotNull();
  }

  @Test
  public void test_entities_are_not_cached_when_the_table_does_not_tell_its_last_update() {
    StaticValueTable table = newTable(DateTimeType.get().nullValue());
    extendWithCache();

    assertThat(newView(table).getVariableEntityCount()).isEqualTo(2);
    assertThat(newView(table).getVariableEntityCount()).isEqualTo(2);
    assertThat(evaluations.get()).isEqualTo(6);
  }

  @Test
  public void test_reconnecting_a_view_computes_its_entities_again() {
    StaticValueTable table = newTable(DateTimeType.get().now());
    extendWithCache();
    DefaultViewManagerImpl manager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
    manager.decorate(new StaticDatasource("ds"));
    manager.addView("ds", newView(table), null, null);

    assertThat(manager.getView("ds", "view").getVariableEntityCount()).isEqualTo(2);
    assertThat(manager.getView("ds", "view").getVariableEntityCount()).isEqualTo(2);
    assertThat(evaluations.get()).isEqualTo(3);

    manager.initView("ds", "view");
    assertThat(manager.getView("ds", "view").getVariableEntityCount()).isEqualTo(2);
    assertThat(evaluations.get()).isEqualTo(6);
  }

  @Test
  public void test_entities_are_kept_in_memory_when_the_cache_cannot_hold_them() throws Exception {
    StaticValueTable table = newTable(DateTimeType.get().now());
    // as a cache on disk does with an entry larger than it can hold: the put is dropped
    MagmaEngine.get().extend(new MagmaCacheExtension(new ConcurrentMapCacheManager("magma-table-entities") {
      @Override
      protected Cache createConcurrentMapCache(String name) {
        return new ConcurrentMapCache(name) {
          @Override
          public void put(Object key, Object value) {
          }
        };
      }
    }));
    View view = newView(table);

    assertThat(view.getVariableEntityCount()).isEqualTo(2);
    ViewVariableEntityProvider.awaitCacheWrites();
    assertThat(view.getVariableEntityCount()).isEqualTo(2);
    assertThat(evaluations.get()).isEqualTo(3);
  }

  @Test
  public void test_removing_the_views_of_a_datasource_evicts_their_entities() throws Exception {
    StaticValueTable table = newTable(DateTimeType.get().now());
    ConcurrentMapCacheManager cacheManager = extendWithCache();
    DefaultViewManagerImpl manager = new DefaultViewManagerImpl(new MemoryViewPersistenceStrategy());
    manager.decorate(new StaticDatasource("ds"));
    manager.addView("ds", newView(table), null, null);
    ValueView view = manager.getView("ds", "view");
    assertThat(view.getVariableEntityCount()).isEqualTo(2);
    ViewVariableEntityProvider.awaitCacheWrites();
    String key = ViewVariableEntityProvider.getTableCacheKey(view);
    assertThat(cacheManager.getCache("magma-table-entities").get(key)).isNotNull();

    // as when a project is deleted: its datasource is unregistered first
    manager.unregisterDatasource("ds");
    manager.removeAllViews("ds");
    assertThat(cacheManager.getCache("magma-table-entities").get(key)).isNull();
  }

  private ConcurrentMapCacheManager extendWithCache() {
    ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("magma-table-entities");
    // serialize, as a cache kept on disk does
    cacheManager.setStoreByValue(true);
    MagmaEngine.get().extend(new MagmaCacheExtension(cacheManager));
    return cacheManager;
  }

  private View newView(StaticValueTable table) {
    View view = View.Builder.newView("view", table).where(new WhereClause() {
      @Override
      public boolean where(ValueSet valueSet) {
        return where(valueSet, null);
      }

      @Override
      public boolean where(ValueSet valueSet, View v) {
        evaluations.incrementAndGet();
        return !"1".equals(valueSet.getVariableEntity().getIdentifier());
      }
    }).build();
    view.setUpdated(DateTimeType.get().valueOf("2026-01-01"));
    view.initialise();
    return view;
  }

  private static StaticValueTable newTable(Value lastUpdate) {
    StaticValueTable table = new StaticValueTable(new StaticDatasource("ds"), "table", ImmutableList.of("1", "2", "3")) {
      @Override
      public Timestamps getTimestamps() {
        return new Timestamps() {
          @Override
          public Value getLastUpdate() {
            return lastUpdate;
          }

          @Override
          public Value getCreated() {
            return lastUpdate;
          }
        };
      }
    };
    table.addVariables(IntegerType.get(), "AGE");
    return table;
  }
}

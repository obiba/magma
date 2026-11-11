package org.obiba.magma.views;

import org.obiba.magma.*;
import org.obiba.magma.lang.VariableEntityList;
import org.obiba.magma.support.PagingVariableEntityProvider;
import org.obiba.magma.support.VariableEntitiesCache;
import org.obiba.magma.transform.BijectiveFunctions;
import org.obiba.magma.views.support.AllClause;
import org.obiba.magma.views.support.NoneClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

class ViewVariableEntityProvider implements PagingVariableEntityProvider {

  private static final Logger log = LoggerFactory.getLogger(ViewVariableEntityProvider.class);

  /**
   * Writes to the cache, which can be on disk, off the request threads, one at a time and in order with the evictions.
   */
  private static final ExecutorService CACHE_WRITER = Executors.newSingleThreadExecutor(
      new ThreadFactoryBuilder().setNameFormat("magma-view-entities-cache").setDaemon(true).build());

  private final View view;

  private VariableEntityList defaultEntities;

  private long timestamp;

  /**
   * Entities being written to the cache, or kept here because the cache could not hold them.
   */
  private VariableEntitiesCache unsaved;

  ViewVariableEntityProvider(View view) {
    this.view = view;
  }

  @NotNull
  @Override
  public String getEntityType() {
    return view.getEntityType();
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getEntityType().equals(entityType);
  }

  @NotNull
  @Override
  public List<VariableEntity> getVariableEntities() {
    if (useWrappedValueTableVariableEntities()) return view.getWrappedValueTable().getVariableEntities();
    MagmaStoreExtension.VariableEntityStore store = getVariableEntityStore();
    if (store != null) return store.getVariableEntities(view);
    VariableEntitiesCache cache = getVariableEntitiesCache();
    if (cache != null) return cache.getEntities();
    return getDefaultVariableEntities();
  }

  @Override
  public List<VariableEntity> getVariableEntities(int offset, int limit) {
    if (useWrappedValueTableVariableEntities()) return view.getWrappedValueTable().getVariableEntities(offset, limit);
    MagmaStoreExtension.VariableEntityStore store = getVariableEntityStore();
    if (store != null) return store.getVariableEntities(view, offset, limit);
    VariableEntitiesCache cache = getVariableEntitiesCache();
    if (cache != null) return getVariableEntitiesPage(cache.getEntities(), offset, limit);
    return getVariableEntitiesPage(getDefaultVariableEntities(), offset, limit);
  }

  @Override
  public boolean hasVariableEntity(VariableEntity entity) {
    if (useWrappedValueTableVariableEntities()) return view.getWrappedValueTable().hasValueSet(entity);
    MagmaStoreExtension.VariableEntityStore store = getVariableEntityStore();
    if (store != null) return store.hasVariableEntity(view, entity);
    VariableEntitiesCache cache = getVariableEntitiesCache();
    if (cache != null) return cache.getEntities().contains(entity);
    return getDefaultVariableEntities().contains(entity);
  }

  @Override
  public int getVariableEntityCount() {
    if (useWrappedValueTableVariableEntities()) return view.getWrappedValueTable().getVariableEntityCount();
    MagmaStoreExtension.VariableEntityStore store = getVariableEntityStore();
    if (store != null) return store.getVariableEntityCount(view);
    VariableEntitiesCache cache = getVariableEntitiesCache();
    if (cache != null) return cache.getEntities().size();
    return getVariableEntities().size();
  }

  private List<VariableEntity> getVariableEntitiesPage(List<VariableEntity> entities, int offset, int limit) {
    int total = entities.size();
    int from = Math.max(offset, 0);
    from = Math.min(from, total);
    int to = limit >= 0 ? from + limit : total;
    to = Math.min(to, total);
    return entities.subList(from, to);
  }

  private synchronized MagmaStoreExtension.VariableEntityStore getVariableEntityStore() {
    if (MagmaEngine.get().hasExtension(MagmaStoreExtension.class)) {
      MagmaStoreExtension extension = MagmaEngine.get().getExtension(MagmaStoreExtension.class);
      if (extension.hasVariableEntityStore()) {
        if (extension.getVariableEntityStore().getVariableEntityCount(view) == 0) {
          List<VariableEntity> entities = loadVariableEntities();
          extension.getVariableEntityStore().saveVariableEntities(view, entities);
        }
        return extension.getVariableEntityStore();
      }
    }
    return null;
  }

  private synchronized VariableEntitiesCache getVariableEntitiesCache() {
    if (MagmaEngine.get().hasExtension(MagmaCacheExtension.class)) {
      MagmaCacheExtension cacheExtension = MagmaEngine.get().getExtension(MagmaCacheExtension.class);
      if (cacheExtension.hasVariableEntitiesCache()) {
        Value fromLastUpdate = view.getWrappedValueTable().getTimestamps().getLastUpdate();
        // a table that does not tell when it was updated could not tell either when the cached entities are stale
        if (fromLastUpdate.isNull()) return null;
        Value viewLastUpdate = view.getTimestamps().getLastUpdate();
        if (unsaved != null && unsaved.isUpToDate(viewLastUpdate)) return unsaved;
        Cache cache = cacheExtension.getVariableEntitiesCache();
        try {
          Cache.ValueWrapper wrapper = cache.get(getTableCacheKey());
          VariableEntitiesCache cached = wrapper == null ? null : (VariableEntitiesCache) wrapper.get();
          // entries written by earlier releases hold a plain list
          if (cached != null && cached.getEntities() instanceof VariableEntityList && cached.isUpToDate(viewLastUpdate))
            return cached;
        } catch (Exception e) {
          // ignore, cache content is wrong
        }
        VariableEntitiesCache eCache = new VariableEntitiesCache(loadVariableEntities(), viewLastUpdate);
        unsaved = eCache;
        String key = getTableCacheKey();
        CACHE_WRITER.execute(() -> save(cache, key, eCache, viewLastUpdate));
        return eCache;
      }
    }
    return null;
  }

  private void save(Cache cache, String key, VariableEntitiesCache eCache, Value viewLastUpdate) {
    try {
      cache.put(key, eCache);
      // reading it back tells whether the cache could hold it, and brings it to memory for the next reads
      Cache.ValueWrapper wrapper = cache.get(key);
      if (wrapper == null || !((VariableEntitiesCache) wrapper.get()).isUpToDate(viewLastUpdate)) {
        log.warn("Entities of {} could not be cached, they are kept in memory", key);
        return;
      }
    } catch (RuntimeException e) {
      log.warn("Entities of {} could not be cached, they are kept in memory", key, e);
      return;
    }
    synchronized (this) {
      if (unsaved == eCache) unsaved = null;
    }
  }

  private String getTableCacheKey() {
    return getTableCacheKey(view);
  }

  static String getTableCacheKey(ValueTable view) {
    return getTableCacheKey(view.getTableReference(), view.getClass());
  }

  static String getTableCacheKey(String tableReference, Class<?> viewClass) {
    return tableReference + ";class=" + viewClass.getName();
  }

  static boolean hasVariableEntitiesCache() {
    return MagmaEngine.get().hasExtension(MagmaCacheExtension.class)
        && MagmaEngine.get().getExtension(MagmaCacheExtension.class).hasVariableEntitiesCache();
  }

  /**
   * Evicts once the writes submitted before are done, so that none of them puts back what is evicted.
   */
  static void evict(String key) {
    if (!hasVariableEntitiesCache()) return;
    Cache cache = MagmaEngine.get().getExtension(MagmaCacheExtension.class).getVariableEntitiesCache();
    try {
      CACHE_WRITER.submit(() -> cache.evict(key)).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      log.warn("Entities of {} could not be evicted from the cache", key, e.getCause());
    }
  }

  /**
   * Waits for the cache writes submitted so far.
   */
  static void awaitCacheWrites() throws InterruptedException, ExecutionException {
    CACHE_WRITER.submit(() -> {}).get();
  }

  private List<VariableEntity> getDefaultVariableEntities() {
    synchronized (this) {
      if (defaultEntities == null || !isCacheUpToDate()) {
        defaultEntities = loadVariableEntities();
        timestamp = new Date().getTime();
      }
    }
    return defaultEntities;
  }

  private boolean isCacheUpToDate() {
    return timestamp > ((Date)view.getTimestamps().getLastUpdate().getValue()).getTime();
  }

  private boolean useWrappedValueTableVariableEntities() {
    // check there is a filter
    if (!(view.getWhereClause() instanceof AllClause)) return false;
    // check there is a mapper
    if (!view.getVariableEntityMappingFunction().equals(BijectiveFunctions.identity())) return false;
    if (view.getWrappedValueTable() instanceof JoinTable) {
      JoinTable join = (JoinTable) view.getWrappedValueTable();
      return join.getOuterTables().size() == 1;
      // TODO could do better, for instance when one table contains all the entities or when entities are distinct accross tables
    }
    return true;
  }

  // the entity list looks entities up by identifier, and keeps doing so once read back from the cache
  VariableEntityList loadVariableEntities() {
    if (!view.hasVariables() || view.getWhereClause() instanceof NoneClause) return new VariableEntityList();
    ValueTable from = view.getWrappedValueTable();
    List<VariableEntity> selected = view.getWhereClause() instanceof AllClause
        ? from.getVariableEntities()
        : view.getWhereClause().filter(from, from.getVariableEntities(), view);
    return selected.stream()
        .map(e -> view.getVariableEntityMappingFunction().apply(e))
        .filter(Objects::nonNull) // can happen depending of mapping strategy
        .collect(Collectors.toCollection(VariableEntityList::new));
  }

}

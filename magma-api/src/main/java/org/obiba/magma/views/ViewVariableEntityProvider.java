package org.obiba.magma.views;

import org.obiba.magma.*;
import org.obiba.magma.lang.VariableEntityList;
import org.obiba.magma.support.PagingVariableEntityProvider;
import org.obiba.magma.support.VariableEntitiesCache;
import org.obiba.magma.transform.BijectiveFunctions;
import org.obiba.magma.views.support.AllClause;
import org.obiba.magma.views.support.NoneClause;
import org.springframework.cache.Cache;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class ViewVariableEntityProvider implements PagingVariableEntityProvider {

  private final View view;

  private VariableEntityList defaultEntities;

  private long timestamp;

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
        cache.put(getTableCacheKey(), eCache);
        return eCache;
      }
    }
    return null;
  }

  private String getTableCacheKey() {
    return getTableCacheKey(view);
  }

  static String getTableCacheKey(ValueTable view) {
    return view.getTableReference() + ";class=" + view.getClass().getName();
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

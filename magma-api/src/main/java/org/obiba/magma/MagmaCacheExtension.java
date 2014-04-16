package org.obiba.magma;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class MagmaCacheExtension implements MagmaEngineExtension {

  private static final long serialVersionUID = -6089615244332195129L;

  private transient CacheManager cacheManager;

  public MagmaCacheExtension() {
  }

  public MagmaCacheExtension(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Override
  public String getName() {
    return "magma-cache";
  }

  @Override
  public void initialise() {

  }

  public boolean hasVariableEntitiesCache() {
    return hasCacheManager() && cacheManager.getCache("magma-table-entities") != null;
  }

  public Cache getVariableEntitiesCache() {
    return cacheManager.getCache("magma-table-entities");
  }

  public boolean hasCacheManager() {
    return cacheManager != null;
  }

  public CacheManager getCacheManager() {
    return cacheManager;
  }

  public void setCacheManager(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }
}

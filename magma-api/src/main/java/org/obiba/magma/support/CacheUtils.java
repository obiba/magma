package org.obiba.magma.support;

import org.obiba.magma.MagmaRuntimeException;
import org.springframework.cache.Cache;

import com.google.common.base.Supplier;

public class CacheUtils {

  public static <T> T getCached(Cache cache, Object key, Supplier<T> supplier) {
    try {
      Cache.ValueWrapper cacheValue = cache.get(key);

      if(cacheValue != null) return (T) cacheValue.get();

      T res = supplier.get();
      cache.put(key, res);
      return res;
    } catch(MagmaRuntimeException e) {
      throw e;
    }
  }
}

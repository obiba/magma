package org.obiba.magma.views;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Simple Map storage. Not for production!
 */
public class MemoryViewPersistenceStrategy implements ViewPersistenceStrategy {

  private final Map<String, Map<String, View>> map = new HashMap<String, Map<String, View>>();

  @Override
  public void writeViews(@Nonnull String datasourceName, @Nonnull Set<View> views) {
    Map<String, View> datasourceViews = getSafeDatasourceViews(datasourceName);
    for(View view : views) {
      datasourceViews.put(view.getName(), view);
    }
  }

  @Override
  public Set<View> readViews(@Nonnull String datasourceName) {
    Map<String, View> datasourceViews = map.get(datasourceName);
    return datasourceViews == null ? Collections.<View>emptySet() : new HashSet<View>(datasourceViews.values());
  }

  private Map<String, View> getSafeDatasourceViews(String datasourceName) {
    Map<String, View> datasourceViews = map.get(datasourceName);
    if(datasourceViews == null) {
      datasourceViews = new HashMap<String, View>();
      map.put(datasourceName, datasourceViews);
    }
    return datasourceViews;
  }

}

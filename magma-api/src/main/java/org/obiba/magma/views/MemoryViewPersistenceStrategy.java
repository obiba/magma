package org.obiba.magma.views;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Simple Map storage. Not for production!
 */
public class MemoryViewPersistenceStrategy implements ViewPersistenceStrategy {

  private final Map<String, Map<String, View>> map = new HashMap<>();

  @Override
  public void initialise() {
  }

  @Override
  public void dispose() {
    map.clear();
  }

  @Override
  public void writeViews(@NotNull String datasourceName, @NotNull Set<View> views, @Nullable String comment) {
    Map<String, View> datasourceViews = getSafeDatasourceViews(datasourceName);
    for(View view : views) {
      datasourceViews.put(view.getName(), view);
    }
  }

  @Override
  public void writeView(@NotNull String datasourceName, @NotNull View view, @Nullable String comment) {
    Map<String, View> datasourceViews = getSafeDatasourceViews(datasourceName);
    datasourceViews.put(view.getName(), view);
  }

  @NotNull
  private Map<String, View> getSafeDatasourceViews(String datasourceName) {
    Map<String, View> datasourceViews = map.get(datasourceName);
    if(datasourceViews == null) {
      datasourceViews = new HashMap<>();
      map.put(datasourceName, datasourceViews);
    }
    return datasourceViews;
  }

  @Override
  public void removeView(@NotNull String datasourceName, @NotNull String viewName) {
    Map<String, View> datasourceViews = map.get(datasourceName);
    if(datasourceViews != null) {
      datasourceViews.remove(viewName);
    }
  }

  @Override
  public void removeViews(String datasourceName) {
    map.remove(datasourceName);
  }

  @Override
  public Set<View> readViews(@NotNull String datasourceName) {
    Map<String, View> datasourceViews = map.get(datasourceName);
    return datasourceViews == null ? Collections.<View>emptySet() : new HashSet<>(datasourceViews.values());
  }

}

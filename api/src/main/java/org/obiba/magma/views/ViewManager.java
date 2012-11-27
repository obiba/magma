package org.obiba.magma.views;

import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;

@SuppressWarnings("UnusedDeclaration")
public interface ViewManager extends Decorator<Datasource> {

  void addView(@Nullable String datasourceName, View view);

  void removeView(@Nullable String datasourceName, String viewName);

  void removeAllViews(@Nullable String datasourceName);

  boolean hasView(@Nullable String datasourceName, String viewName);

  View getView(@Nullable String datasourceName, String viewName);

  void addViews(@Nullable String datasource, Set<View> views);
}

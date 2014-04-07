package org.obiba.magma.views;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;

import edu.umd.cs.findbugs.annotations.Nullable;

@SuppressWarnings("UnusedDeclaration")
public interface ViewManager extends Decorator<Datasource> {

  void addView(@NotNull String datasourceName, @NotNull View view, @Nullable String comment);

  void removeView(@NotNull String datasourceName, @NotNull String viewName);

  void removeAllViews(@NotNull String datasourceName);

  void unregisterDatasource(@NotNull String datasourceName);

  boolean hasView(@NotNull String datasourceName, @NotNull String viewName);

  View getView(@NotNull String datasourceName, @NotNull String viewName);

  void addViews(@NotNull String datasource, Set<View> views, @Nullable String comment);
}

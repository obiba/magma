package org.obiba.magma.views;

import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;

import edu.umd.cs.findbugs.annotations.Nullable;

@SuppressWarnings("UnusedDeclaration")
public interface ViewManager extends Decorator<Datasource> {

  void addView(@Nonnull String datasourceName, @Nonnull View view, @Nullable String comment);

  void removeView(@Nonnull String datasourceName, @Nonnull String viewName);

  void removeAllViews(@Nonnull String datasourceName);

  boolean hasView(@Nonnull String datasourceName, @Nonnull String viewName);

  View getView(@Nonnull String datasourceName, @Nonnull String viewName);

  void addViews(@Nonnull String datasource, Set<View> views, @Nullable String comment);
}

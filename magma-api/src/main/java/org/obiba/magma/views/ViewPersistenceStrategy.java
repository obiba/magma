package org.obiba.magma.views;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface ViewPersistenceStrategy extends Initialisable, Disposable {

  /**
   * Add or update the set of views for the datasource.
   *
   * @param datasourceName
   * @param views
   */
  void writeViews(@NotNull String datasourceName, @NotNull Set<View> views, @Nullable String comment);

  /**
   * Add or update the view for the datasource.
   *
   * @param datasourceName
   * @param view
   */
  void writeView(@NotNull String datasourceName, @NotNull View view, @Nullable String comment);

  /**
   * Remove a specified view of a datasource. Silently ignore if no such view exists.
   *
   * @param datasourceName
   * @param viewName
   */
  void removeView(@NotNull String datasourceName, @NotNull String viewName);

  /**
   * Remove all views of a datasource.
   *
   * @param datasourceName
   */
  void removeViews(String datasourceName);

  /**
   * Read all views of the datasource.
   *
   * @param datasourceName
   * @return
   */
  Set<View> readViews(@NotNull String datasourceName);

}

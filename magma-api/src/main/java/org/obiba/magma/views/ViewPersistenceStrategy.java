package org.obiba.magma.views;

import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.magma.Disposable;
import org.obiba.magma.Initialisable;

public interface ViewPersistenceStrategy extends Initialisable, Disposable {

  /**
   * Add or update the set of views for the datasource.
   * @param datasourceName
   * @param views
   */
  void writeViews(@Nonnull String datasourceName, @Nonnull Set<View> views);

  /**
   * Add or update the view for the datasource.
   * @param datasourceName
   * @param view
   */
  void writeView(@Nonnull String datasourceName, @Nonnull View view);

  /**
   * Remove a specified view of a datasource. Silently ignore if no such view exists.
   * @param datasourceName
   * @param viewName
   */
  void removeView(@Nonnull String datasourceName, @Nonnull String viewName);

  /**
   * Remove all views of a datasource.
   * @param datasourceName
   */
  void removeViews(String datasourceName);

  /**
   * Read all views of the datasource.
   * @param datasourceName
   * @return
   */
  Set<View> readViews(@Nonnull String datasourceName);

}

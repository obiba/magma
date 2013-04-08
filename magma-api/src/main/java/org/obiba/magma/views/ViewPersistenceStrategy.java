package org.obiba.magma.views;

import java.util.Set;

import javax.annotation.Nonnull;

public interface ViewPersistenceStrategy {

  void writeViews(@Nonnull String datasourceName, @Nonnull Set<View> views);

  Set<View> readViews(@Nonnull String datasourceName);

}

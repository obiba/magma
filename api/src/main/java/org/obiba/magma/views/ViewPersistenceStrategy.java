package org.obiba.magma.views;

import java.util.Set;

public interface ViewPersistenceStrategy {

  void writeViews(String datasourceName, Set<View> views);

  Set<View> readViews(String datasourceName);

}

package org.obiba.magma.views;

import java.util.Set;

public interface ViewPersistenceStrategy {

  public void writeViews(String datasourceName, Set<View> views);

  public Set<View> readViews(String datasourceName);

}

package org.obiba.magma.views;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.Decorator;

public interface ViewManager extends Decorator<Datasource> {

  public void addView(String datasourceName, View view);

  public void removeView(String datasourceName, String viewName);

  public boolean hasView(String datasourceName, String viewName);

  public View getView(String datasourceName, String viewName);

  public void addViews(String datasource, Set<View> views);
}

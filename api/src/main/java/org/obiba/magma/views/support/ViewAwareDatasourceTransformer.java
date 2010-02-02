package org.obiba.magma.views.support;

import java.util.HashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceTransformer;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewAwareDatasource;

public class ViewAwareDatasourceTransformer implements DatasourceTransformer {
  //
  // Instance Variables
  //

  private String name;

  private Set<View> views;

  //
  // DatasourceTransformer Methods
  //

  @Override
  public Datasource transform(Datasource datasource) {
    if(views != null) {
      return new ViewAwareDatasource((name != null ? name : datasource.getName()), datasource, views);
    }
    return datasource;
  }

  //
  // Methods
  //

  public void setName(String name) {
    this.name = name;
  }

  public void setViews(Set<View> views) {
    this.views = new HashSet<View>();
    if(views != null) {
      this.views.addAll(views);
    }
  }
}

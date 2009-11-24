package org.obiba.magma.spring;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.springframework.beans.factory.InitializingBean;

public class DatasourceRegistratingFactoryBean implements InitializingBean {

  private Set<Datasource> datasources;

  public void setDatasources(Set<Datasource> datasources) {
    this.datasources = datasources;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    MagmaEngine engine = MagmaEngine.get();
    for(Datasource ds : datasources) {
      engine.addDatasource(ds);
    }
  }

}

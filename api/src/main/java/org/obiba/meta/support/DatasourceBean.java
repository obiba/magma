package org.obiba.meta.support;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.Datasource;

public class DatasourceBean implements Datasource {

  private String name;

  private String type;

  private Properties properties;

  private Set<Collection> collections;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public Properties getProperties() {
    return properties;
  }

  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  @Override
  public Set<Collection> getCollections() {
    return Collections.unmodifiableSet(collections);
  }

}

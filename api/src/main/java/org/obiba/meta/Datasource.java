package org.obiba.meta;

import java.util.Properties;
import java.util.Set;

public interface Datasource {

  public String getName();

  public String getType();

  public Set<Collection> getCollections();

  public Properties getProperties();

  public void setProperties(Properties properties);

}

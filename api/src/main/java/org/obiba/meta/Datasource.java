package org.obiba.meta;

import java.util.Properties;
import java.util.Set;

public interface Datasource extends Initialisable, ValueSetConnectionFactory {

  public String getName();

  public String getType();

  public Set<Collection> getCollections();

  public Properties getProperties();

  public void setProperties(Properties properties);

}

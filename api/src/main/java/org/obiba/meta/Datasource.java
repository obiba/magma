package org.obiba.meta;

import java.util.Properties;
import java.util.Set;

public interface Datasource extends Initialisable {

  public String getName();

  public String getType();

  public Set<Collection> getCollections();

  public Properties getProperties();

}

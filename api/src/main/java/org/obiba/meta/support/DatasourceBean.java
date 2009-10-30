package org.obiba.meta.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.obiba.meta.Collection;
import org.obiba.meta.Datasource;
import org.obiba.meta.Initialisable;

import com.google.common.collect.Iterables;

public class DatasourceBean implements Datasource {

  private String name;

  private String type;

  private Properties properties;

  private Set<Collection> collections = new HashSet<Collection>();

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

  @Override
  public void initialise() {
    for(Initialisable initialisable : Iterables.filter(getCollections(), Initialisable.class)) {
      initialisable.initialise();
    }
  }

  public void addCollection(Collection collection) {
    collections.add(collection);
  }

}

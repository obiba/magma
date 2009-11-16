package org.obiba.magma.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.obiba.magma.Collection;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;

import com.google.common.collect.Iterables;

public abstract class AbstractDatasource implements Datasource {

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
  public Set<Collection> getCollections() {
    return Collections.unmodifiableSet(collections);
  }

  @Override
  public void initialise() {

    for(String collection : getCollectionNames()) {
      collections.add(initialiseCollection(collection));
    }

    for(Initialisable initialisable : Iterables.filter(getCollections(), Initialisable.class)) {
      initialisable.initialise();
    }
  }

  protected abstract Set<String> getCollectionNames();

  protected abstract Collection initialiseCollection(String collection);
}

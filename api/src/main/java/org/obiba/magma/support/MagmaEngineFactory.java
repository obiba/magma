package org.obiba.magma.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.NoSuchDatasourceException;

import com.google.common.collect.Sets;

public class MagmaEngineFactory {

  private Set<MagmaEngineExtension> extensions = Sets.newLinkedHashSet();

  /**
   * Expose concrete type to force xstream to deserialize using this type. This will keep the order in which factories
   * appear in the xml file
   */
  private ArrayList<DatasourceFactory> factories = new ArrayList<DatasourceFactory>();

  private LinkedHashSet<Datasource> datasources = Sets.newLinkedHashSet();

  public MagmaEngineFactory() {
  }

  public MagmaEngineFactory withExtension(MagmaEngineExtension extension) {
    this.extensions.add(extension);
    return this;
  }

  public MagmaEngineFactory withFactory(DatasourceFactory factory) {

    // Replace the factory if already exist.
    boolean factoryExist = false;
    for(DatasourceFactory factoryInList : factories) {
      if(factoryInList.getName().equals(factory.getName())) {
        factories.add(factories.indexOf(factoryInList), factory);
        factories.remove(factoryInList);
        factoryExist = true;
        break;
      }
    }

    if(!factoryExist) {
      factories.add(factory);
    }

    return this;
  }

  public void removeFactory(String name) {
    for(DatasourceFactory factory : factories) {
      if(factory.getName().equals(name)) {
        factories.remove(factory);
        break;
      }
    }
  }

  public boolean hasDatasourceFactory(String name) {
    for(DatasourceFactory factory : factories) {
      if(factory.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  public DatasourceFactory getDatasourceFactory(String name) {
    for(DatasourceFactory factory : factories) {
      if(factory.getName().equals(name)) {
        return factory;
      }
    }
    throw new NoSuchDatasourceException("Datasource factory could not be found: " + name);
  }

  public MagmaEngineFactory withDatasource(Datasource datasource) {
    this.datasources.add(datasource);
    return this;
  }

  public void initialize(MagmaEngine engine) {
    for(MagmaEngineExtension extension : extensions) {
      engine.extend(extension);
    }

    for(DatasourceFactory factory : factories) {
      engine.addDatasource(factory);
    }

    for(Datasource datasource : datasources) {
      engine.addDatasource(datasource);
    }
  }

  public Set<MagmaEngineExtension> extensions() {
    return Collections.unmodifiableSet(extensions);
  }

  public Set<DatasourceFactory> factories() {
    return Collections.unmodifiableSet(Sets.newLinkedHashSet(factories));
  }

}

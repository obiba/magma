package org.obiba.magma.support;

import java.util.Collections;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;

import com.google.common.collect.Sets;

public class MagmaEngineFactory {

  private Set<MagmaEngineExtension> extensions = Sets.newLinkedHashSet();

  private Set<DatasourceFactory> factories = Sets.newLinkedHashSet();

  private Set<Datasource> datasources = Sets.newLinkedHashSet();

  public MagmaEngineFactory() {
  }

  public MagmaEngineFactory withExtension(MagmaEngineExtension extension) {
    this.extensions.add(extension);
    return this;
  }

  public MagmaEngineFactory withFactory(DatasourceFactory factory) {
    this.factories.add(factory);
    return this;
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
    return Collections.unmodifiableSet(factories);
  }

}

package org.obiba.magma.support;

import java.util.Collections;
import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.MagmaRuntimeException;

import com.google.common.collect.Sets;

public class MagmaEngineFactory {

  private String engineClass = MagmaEngine.class.getName();

  private Set<MagmaEngineExtension> extensions = Sets.newLinkedHashSet();

  private Set<DatasourceFactory<?>> factories = Sets.newLinkedHashSet();

  private Set<Datasource> datasources = Sets.newLinkedHashSet();

  public MagmaEngineFactory() {
  }

  public MagmaEngineFactory withEngineClass(String engineClass) {
    this.engineClass = engineClass;
    return this;
  }

  public MagmaEngineFactory withExtension(MagmaEngineExtension extension) {
    this.extensions.add(extension);
    return this;
  }

  public MagmaEngineFactory withFactory(DatasourceFactory<?> factory) {
    this.factories.add(factory);
    return this;
  }

  public MagmaEngineFactory withDatasource(Datasource datasource) {
    this.datasources.add(datasource);
    return this;
  }

  public MagmaEngine create() {
    MagmaEngine engine = newEngineInstance(engineClass);
    for(MagmaEngineExtension extension : extensions) {
      engine.extend(extension);
    }

    for(DatasourceFactory<?> factory : factories) {
      engine.addDatasource(factory);
    }

    for(Datasource datasource : datasources) {
      engine.addDatasource(datasource);
    }

    return engine;
  }

  public String getEngineClass() {
    return engineClass;
  }

  public Set<MagmaEngineExtension> extensions() {
    return Collections.unmodifiableSet(extensions);
  }

  protected MagmaEngine newEngineInstance(String engineClass) {
    try {
      return (MagmaEngine) Class.forName(engineClass).newInstance();
    } catch(InstantiationException e) {
      throw new MagmaRuntimeException(e);
    } catch(IllegalAccessException e) {
      throw new MagmaRuntimeException(e);
    } catch(ClassNotFoundException e) {
      throw new MagmaRuntimeException(e);
    }
  }
}

package org.obiba.magma;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class MagmaEngine {

  /** Keeps a reference on all singletons */
  private static List<Object> singletons;

  private static MagmaEngine instance;

  private ValueTypeFactory valueTypeFactory;

  private Set<MagmaEngineExtension> extensions = Sets.newHashSet();

  private Set<Datasource> datasources = Sets.newHashSet();

  public MagmaEngine() {
    if(instance != null) {
      throw new IllegalStateException("MetaEngine already instanciated. Only one instance of MetaEngine should be instantiated.");
    }
    instance = this;

    singletons = new LinkedList<Object>();
    valueTypeFactory = new ValueTypeFactory();
  }

  public static MagmaEngine get() {
    if(instance == null) {
      throw new IllegalStateException("MagmaEngine not instanciated. Make sure you instantiate the engine before accessing other static methods in the api.");
    }
    return instance;
  }

  public MagmaEngine extend(MagmaEngineExtension extension) {
    Initialisables.initialise(extension);
    extensions.add(extension);
    return this;
  }

  public <T extends MagmaEngineExtension> T getExtension(Class<T> extensionType) {
    try {
      return Iterables.getOnlyElement(Iterables.filter(extensions, extensionType));
    } catch(NoSuchElementException e) {
      throw new MagmaRuntimeException("No extension of type '" + extensionType + "' was registered.");
    }
  }

  ValueTypeFactory getValueTypeFactory() {
    return valueTypeFactory;
  }

  public Set<Datasource> getDatasources() {
    return ImmutableSet.copyOf(datasources);
  }

  public Datasource getDatasource(final String name) {
    try {
      return Iterables.find(datasources, new Predicate<Datasource>() {
        @Override
        public boolean apply(Datasource input) {
          return name.equals(input.getName());
        }
      });
    } catch(NoSuchElementException e) {
      throw new NoSuchDatasourceException(name);
    }
  }

  public <T extends Datasource> T addDatasource(final T datasource) {
    Initialisables.initialise(datasource);
    datasources.add(datasource);
    return datasource;
  }

  public <T extends Datasource> T addDatasource(final DatasourceFactory<T> factory) {
    Initialisables.initialise(factory);
    return addDatasource(factory.create());
  }

  public void removeDatasource(final Datasource datasource) {
    datasources.remove(datasource);
    Disposables.dispose(datasource);
  }

  public <T> WeakReference<T> registerInstance(final T singleton) {
    singletons.add(singleton);
    return new WeakReference<T>(singleton);
  }

  public void shutdown() {
    for(Datasource ds : datasources) {
      Disposables.silentlyDispose(ds);
    }
    singletons = null;
    instance = null;
  }

}

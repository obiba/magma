package org.obiba.magma;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

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
      throw new IllegalStateException("MetaEngine not instanciated. Make sure you instantiate the engine before accessing other static methods in the api.");
    }
    return instance;
  }

  public MagmaEngine extend(MagmaEngineExtension extension) {
    try {
      extension.initialise();
    } catch(MagmaRuntimeException e) {
      throw e;
    } catch(RuntimeException e) {
      throw new MagmaRuntimeException(e);
    }
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

  public ValueTypeFactory getValueTypeFactory() {
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

  public void addDatasource(Datasource datasource) {
    datasource.initialise();
    datasources.add(datasource);
  }

  public void removeDatasource(Datasource datasource) {
    datasources.remove(datasource);
    datasource.dispose();
  }

  public <T> WeakReference<T> registerInstance(T singleton) {
    singletons.add(singleton);
    return new WeakReference<T>(singleton);
  }

  public void shutdown() {
    for(Datasource ds : datasources) {
      try {
        ds.dispose();
      } catch(RuntimeException e) {
        // Ignore
      }
    }
    singletons = null;
    instance = null;
  }

}

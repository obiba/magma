package org.obiba.magma;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.concurrent.LockManager;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class MagmaEngine implements DatasourceRegistry {

  /** Keeps a reference on all singletons */
  private static List<Object> singletons;

  private static MagmaEngine instance;

  private ValueTypeFactory valueTypeFactory;

  private DatasourceRegistry datasourceRegistry = new DefaultDatasourceRegistry();

  private Set<MagmaEngineExtension> extensions = Sets.newHashSet();

  private LockManager lockManager = new LockManager();

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
    if(extension == null) throw new IllegalArgumentException("extension cannot be null");
    if(hasExtension(extension.getClass()) == false) {
      Initialisables.initialise(extension);
      extensions.add(extension);
    }
    return this;
  }

  public boolean hasExtension(Class<? extends MagmaEngineExtension> extensionType) {
    try {
      Iterables.getOnlyElement(Iterables.filter(extensions, extensionType));
      return true;
    } catch(NoSuchElementException e) {
      return false;
    }
  }

  public <T extends MagmaEngineExtension> T getExtension(Class<T> extensionType) {
    try {
      return Iterables.getOnlyElement(Iterables.filter(extensions, extensionType));
    } catch(NoSuchElementException e) {
      throw new MagmaRuntimeException("No extension of type '" + extensionType + "' was registered.");
    }
  }

  public DatasourceRegistry getDatasourceRegistry() {
    return datasourceRegistry;
  }

  public void decorate(Decorator<DatasourceRegistry> registryDecorator) {
    this.datasourceRegistry = registryDecorator.decorate(this.datasourceRegistry);
  }

  @Override
  public Datasource getDatasource(String name) {
    return getDatasourceRegistry().getDatasource(name);
  }

  @Override
  public Datasource addDatasource(Datasource datasource) {
    return getDatasourceRegistry().addDatasource(datasource);
  }

  public Datasource addDatasource(DatasourceFactory factory) {
    return getDatasourceRegistry().addDatasource(factory);
  }

  public void addDecorator(Decorator<Datasource> decorator) {
    getDatasourceRegistry().addDecorator(decorator);
  }

  public String addTransientDatasource(DatasourceFactory factory) {
    return getDatasourceRegistry().addTransientDatasource(factory);
  }

  public Set<Datasource> getDatasources() {
    return getDatasourceRegistry().getDatasources();
  }

  public Datasource getTransientDatasourceInstance(String uid) {
    return getDatasourceRegistry().getTransientDatasourceInstance(uid);
  }

  public boolean hasDatasource(String name) {
    return getDatasourceRegistry().hasDatasource(name);
  }

  public boolean hasTransientDatasource(String uid) {
    return getDatasourceRegistry().hasTransientDatasource(uid);
  }

  public void removeDatasource(Datasource datasource) {
    getDatasourceRegistry().removeDatasource(datasource);
  }

  public void removeTransientDatasource(String uid) {
    getDatasourceRegistry().removeTransientDatasource(uid);
  }

  ValueTypeFactory getValueTypeFactory() {
    return valueTypeFactory;
  }

  public void lock(Set<String> lockNames) throws InterruptedException {
    lockManager.lock(lockNames);
  }

  public void unlock(Set<String> lockNames) {
    lockManager.unlock(lockNames, true);
  }

  public <T> WeakReference<T> registerInstance(final T singleton) {
    singletons.add(singleton);
    return new WeakReference<T>(singleton);
  }

  public void shutdown() {
    for(Disposable d : Iterables.filter(this.extensions, Disposable.class)) {
      Disposables.silentlyDispose(d);
    }
    Disposables.silentlyDispose(datasourceRegistry);
    singletons = null;
    instance = null;
  }

}

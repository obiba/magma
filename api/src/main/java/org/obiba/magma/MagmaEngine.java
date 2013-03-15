package org.obiba.magma;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.concurrent.LockManager;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.ValueTableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod", "StaticNonFinalField", "UnusedDeclaration" })
public class MagmaEngine implements DatasourceRegistry {

  private final static Logger log = LoggerFactory.getLogger(MagmaEngine.class);

  /**
   * Keeps a reference on all singletons
   */
  private static Set<Object> singletons;

  @Nullable
  private static MagmaEngine instance;

  private ValueTypeFactory valueTypeFactory;

  private DatasourceRegistry datasourceRegistry = new DefaultDatasourceRegistry();

  private Set<MagmaEngineExtension> extensions = Sets.newHashSet();

  private LockManager lockManager = new LockManager();

  public MagmaEngine() {
    if(instance != null) {
      throw new IllegalStateException(
          "MagmaEngine already instanciated. Only one instance of MagmaEngine should be instantiated.");
    }
    instance = this;

    singletons = new LinkedHashSet<Object>();
    valueTypeFactory = new ValueTypeFactory();
  }

  @Nonnull
  public static MagmaEngine get() {
    if(instance == null) {
      log.warn("Instanciating a new MagmaEngine without any extensions.");
      new MagmaEngine();
    }
    return instance;
  }

  public MagmaEngine extend(MagmaEngineExtension extension) {
    if(extension == null) throw new IllegalArgumentException("extension cannot be null");
    if(!hasExtension(extension.getClass())) {
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

  /**
   * Returns true if the {@code MagmaEngine} has a instance of {@code MagmaEngineExtension} whose name equals the name
   * provided.
   *
   * @param name
   * @return
   */
  public boolean hasExtension(String name) {
    for(MagmaEngineExtension e : extensions) {
      if(e.getName().equals(name)) return true;
    }
    return false;
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
    datasourceRegistry = registryDecorator.decorate(datasourceRegistry);
  }

  @Override
  public ValueTableReference createReference(String reference) {
    return getDatasourceRegistry().createReference(reference);
  }

  @Override
  public Datasource getDatasource(String name) {
    return getDatasourceRegistry().getDatasource(name);
  }

  @Override
  public Datasource addDatasource(Datasource datasource) {
    return getDatasourceRegistry().addDatasource(datasource);
  }

  @Override
  public Datasource addDatasource(DatasourceFactory factory) {
    return getDatasourceRegistry().addDatasource(factory);
  }

  @Override
  public void addDecorator(Decorator<Datasource> decorator) {
    getDatasourceRegistry().addDecorator(decorator);
  }

  @Override
  public String addTransientDatasource(DatasourceFactory factory) {
    return getDatasourceRegistry().addTransientDatasource(factory);
  }

  @Override
  public Set<Datasource> getDatasources() {
    return getDatasourceRegistry().getDatasources();
  }

  @Override
  public Datasource getTransientDatasourceInstance(String uid) {
    return getDatasourceRegistry().getTransientDatasourceInstance(uid);
  }

  @Override
  public boolean hasDatasource(String name) {
    return getDatasourceRegistry().hasDatasource(name);
  }

  @Override
  public boolean hasTransientDatasource(String uid) {
    return getDatasourceRegistry().hasTransientDatasource(uid);
  }

  @Override
  public void removeDatasource(Datasource datasource) {
    getDatasourceRegistry().removeDatasource(datasource);
  }

  @Override
  public void removeTransientDatasource(@Nullable String uid) {
    getDatasourceRegistry().removeTransientDatasource(uid);
  }

  ValueTypeFactory getValueTypeFactory() {
    return valueTypeFactory;
  }

  public void lock(Collection<String> lockNames) throws InterruptedException {
    lockManager.lock(lockNames);
  }

  public void unlock(Iterable<String> lockNames) {
    lockManager.unlock(lockNames, true);
  }

  public <T> WeakReference<T> registerInstance(T singleton) {
    singletons.add(singleton);
    return new WeakReference<T>(singleton);
  }

  public void shutdown() {
    for(Disposable d : Iterables.filter(extensions, Disposable.class)) {
      Disposables.silentlyDispose(d);
    }
    Disposables.silentlyDispose(datasourceRegistry);
    singletons = null;
    instance = null;
  }

}

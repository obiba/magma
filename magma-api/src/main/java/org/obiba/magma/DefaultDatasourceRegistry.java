package org.obiba.magma;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.ValueTableReference;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DefaultDatasourceRegistry implements DatasourceRegistry, Disposable {

  private final Map<String, Datasource> datasources = Maps.newHashMap();

  private final Map<String, DatasourceFactory> transientDatasourceFactories = Maps.newHashMap();

  private final Map<String, Datasource> transientDatasources = Maps.newHashMap();

  private final Set<Decorator<Datasource>> decorators = Sets.newHashSet();

  @Override
  public void dispose() {
    for(Datasource ds : datasources.values()) {
      Disposables.silentlyDispose(ds);
    }
    for(Decorator<Datasource> decorator : decorators) {
      Disposables.silentlyDispose(decorator);
    }
  }

  @Override
  public ValueTableReference createReference(String reference) {
    return new ValueTableReference(reference);
  }

  @Override
  public Set<Datasource> getDatasources() {
    return ImmutableSet.copyOf(datasources.values());
  }

  @Override
  public Datasource getDatasource(String name) throws NoSuchDatasourceException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "datasource name cannot be null or empty");
    Datasource datasource = datasources.get(name);
    if(datasource == null) {
      throw new NoSuchDatasourceException(name);
    }
    return datasource;
  }

  @Override
  public boolean hasDatasource(String name) {
    return datasources.containsKey(name);
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void addDecorator(@NotNull Decorator<Datasource> decorator) {
    if(decorator == null) throw new MagmaRuntimeException("decorator cannot be null.");
    Initialisables.initialise(decorator);
    decorators.add(decorator);

    for(Datasource datasource : datasources.values()) {
      datasources.put(datasource.getName(), decorator.decorate(datasource));
    }
  }

  @Override
  public Datasource addDatasource(Datasource datasource) {
    // Repeatedly added datasources are silently ignored. They cannot be added to the set more than once.
    if(!datasources.containsValue(datasource)) {
      Datasource existing = datasources.get(datasource.getName());
      if(existing != null) {
        // Unique datasources with identical names cause exceptions.
        throw new DuplicateDatasourceNameException(existing, datasource);
      }

      Datasource decorated = decorateDatasource(datasource);
      Initialisables.initialise(decorated);
      datasources.put(decorated.getName(), decorated);
      return decorated;
    }
    return datasource;
  }

  private Datasource decorateDatasource(Datasource datasource) {
    Datasource decorated = datasource;
    for(Decorator<Datasource> decorator : decorators) {
      decorated = decorator.decorate(decorated);
    }
    return decorated;
  }

  @Override
  public Datasource addDatasource(DatasourceFactory factory) {
    Initialisables.initialise(factory);
    return addDatasource(factory.create());
  }

  @Override
  public void removeDatasource(Datasource datasource) {
    datasources.remove(datasource.getName());
    Disposables.dispose(datasource);
  }

  /**
   * Register a new transient datasource.
   *
   * @param factory
   * @return a unique identifier that can be used to obtain the registered factory
   */
  @Override
  public String addTransientDatasource(DatasourceFactory factory) {
    String uid = randomTransientDatasourceName();
    while(hasTransientDatasource(uid)) {
      uid = randomTransientDatasourceName();
    }
    factory.setName(uid);
    Initialisables.initialise(factory);
    transientDatasourceFactories.put(factory.getName(), factory);
    return factory.getName();
  }

  /**
   * Check if a transient datasource is registered with given identifier.
   *
   * @param uid
   * @return true when uid is associated with a DatasourceFactory instance
   */
  @Override
  public boolean hasTransientDatasource(String uid) {
    return transientDatasourceFactories.containsKey(uid);
  }

  /**
   * Dispose & remove the transient datasource & factory identified by uid (ignore if none is found).
   *
   * @param uid
   */
  @Override
  public void removeTransientDatasource(@Nullable String uid) {
    DatasourceFactory factory = transientDatasourceFactories.get(uid);
    if(factory != null) {
      Disposables.dispose(factory);
      transientDatasourceFactories.remove(uid);
    }
    Datasource datasource = transientDatasources.get(uid);
    if(datasource != null) {
      Disposables.dispose(datasource);
      transientDatasources.remove(uid);
    }
  }

  /**
   * Returns a new (initialized) instance of Datasource obtained by calling DatasourceFactory.create() associated with
   * uid.
   *
   * @param uid
   * @return datasource item
   */
  @Override
  public Datasource getTransientDatasourceInstance(String uid) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(uid), "uid cannot be null or empty");
    DatasourceFactory factory = transientDatasourceFactories.get(uid);
    if(factory == null) {
      throw new NoSuchDatasourceException(uid);
    }
    Datasource datasource = transientDatasources.get(uid);
    if(datasource == null) {
      datasource = factory.create();
      Initialisables.initialise(datasource);
      datasource = decorateDatasource(datasource);
      transientDatasources.put(uid, datasource);
    }
    return datasource;
  }

  /**
   * Generate a random name.
   *
   * @return
   */
  @VisibleForTesting
  String randomTransientDatasourceName() {
    return UUID.randomUUID().toString();
  }

}

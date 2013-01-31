package org.obiba.magma;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.ValueTableReference;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class DefaultDatasourceRegistry implements DatasourceRegistry, Disposable {

  private Set<Datasource> datasources = Sets.newHashSet();

  private Set<DatasourceFactory> transientDatasources = Sets.newHashSet();

  private Set<Decorator<Datasource>> decorators = Sets.newHashSet();

  @Override
  public void dispose() {
    for(Datasource ds : datasources) {
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
    return ImmutableSet.copyOf(datasources);
  }

  @Override
  public Datasource getDatasource(final String name) throws NoSuchDatasourceException {
    if(name == null) throw new IllegalArgumentException("datasource name cannot be null");
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

  @Override
  public boolean hasDatasource(String name) {
    for(Datasource d : datasources) {
      if(d.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void addDecorator(final Decorator<Datasource> decorator) {
    if(decorator == null) throw new MagmaRuntimeException("decorator cannot be null.");
    Initialisables.initialise(decorator);
    decorators.add(decorator);

    this.datasources = Sets.newHashSet(Iterables.transform(this.datasources, new Function<Datasource, Datasource>() {

      @Override
      public Datasource apply(Datasource input) {
        return decorator.decorate(input);
      }
    }));
  }

  @Override
  public Datasource addDatasource(Datasource datasource) {
    // Repeatedly added datasources are silently ignored. They cannot be added to the set more than once.
    if(!datasources.contains(datasource)) {
      for(Datasource ds : datasources) {
        if(ds.getName().equals(datasource.getName())) {
          // Unique datasources with identical names cause exceptions.
          throw new DuplicateDatasourceNameException(ds, datasource);
        }
      }

      for(Decorator<Datasource> decorator : decorators) {
        datasource = decorator.decorate(datasource);
      }

      Initialisables.initialise(datasource);
      datasources.add(datasource);
    }
    return datasource;
  }

  @Override
  public Datasource addDatasource(DatasourceFactory factory) {
    Initialisables.initialise(factory);
    return addDatasource(factory.create());
  }

  @Override
  public void removeDatasource(Datasource datasource) {
    datasources.remove(datasource);
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
    transientDatasources.add(factory);

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
    return getTransientDatasource(uid) != null;
  }

  /**
   * Remove the transient datasource identified by uid (ignore if none is found).
   *
   * @param uid
   */
  @Override
  public void removeTransientDatasource(String uid) {
    DatasourceFactory factory = getTransientDatasource(uid);
    if(factory != null) {
      transientDatasources.remove(factory);
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
    DatasourceFactory factory = getTransientDatasource(uid);
    Datasource datasource = null;
    if(factory != null) {
      datasource = factory.create();
      Initialisables.initialise(datasource);
    } else {
      throw new NoSuchDatasourceException(uid);
    }

    for(Decorator<Datasource> decorator : decorators) {
      datasource = decorator.decorate(datasource);
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

  /**
   * Look for a datasource factory with given identifier.
   *
   * @param uid
   * @return null if not found
   */
  private DatasourceFactory getTransientDatasource(String uid) {
    if(uid == null) throw new IllegalArgumentException("uid cannot be null.");
    DatasourceFactory foundFactory = null;
    for(DatasourceFactory factory : transientDatasources) {
      if(factory.getName().equals(uid)) {
        foundFactory = factory;
        break;
      }
    }
    return foundFactory;
  }
}

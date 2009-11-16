package org.obiba.magma;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MagmaEngine {

  /** Keeps a reference on all singletons */
  private static List<Object> singletons;

  private static MagmaEngine instance;

  private ValueTypeFactory valueTypeFactory;

  private Set<Datasource> datasources = new HashSet<Datasource>();

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

  public MagmaEngine extend(Initialisable extension) {
    try {
      extension.initialise();
    } catch(RuntimeException e) {

    }
    return this;
  }

  public ValueTypeFactory getValueTypeFactory() {
    return valueTypeFactory;
  }

  public VariableValueSource lookupVariable(String entityType, String collection, String name) throws NoSuchCollectionException, NoSuchVariableException {
    return lookupCollection(collection).getVariableValueSource(entityType, name);
  }

  public VariableValueSource lookupVariable(String entityType, String name) throws NoSuchCollectionException, NoSuchVariableException {
    int index = name.indexOf(':');
    if(index > -1) {
      String collection = name.substring(0, index);
      String variableName = name.substring(index + 1);
      return lookupVariable(entityType, collection, variableName);
    }
    throw new NoSuchVariableException(name);
  }

  public Collection lookupCollection(String name) throws NoSuchCollectionException {
    for(Datasource ds : datasources) {
      for(Collection c : ds.getCollections()) {
        if(c.getName().equals(name)) {
          return c;
        }
      }
    }
    throw new NoSuchCollectionException(name);
  }

  public void addDatasource(Datasource datasource) {
    datasource.initialise();
    datasources.add(datasource);
  }

  public <T> WeakReference<T> registerInstance(T singleton) {
    singletons.add(singleton);
    return new WeakReference<T>(singleton);
  }

  public void shutdown() {
    singletons = null;
    instance = null;
  }

}

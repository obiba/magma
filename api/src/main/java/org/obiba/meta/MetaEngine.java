package org.obiba.meta;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class MetaEngine {

  /** Keeps a reference on all singletons */
  private static List<Object> singletons = new LinkedList<Object>();

  private static MetaEngine instance;

  private ValueFactory valueFactory = new ValueFactory();

  private ValueTypeFactory valueTypeFactory = new ValueTypeFactory();

  public MetaEngine() {
    if(instance != null) {
      throw new IllegalStateException("MetaEngine already instanciated. Only one instance of MetaEngine should be instantiated.");
    }
    instance = this;
  }

  public static MetaEngine get() {
    return instance;
  }

  public ValueFactory getValueFactory() {
    return valueFactory;
  }

  public ValueTypeFactory getValueTypeFactory() {
    return valueTypeFactory;
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

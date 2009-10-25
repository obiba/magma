package org.obiba.meta;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class MetaEngine {

  private static List<Object> singletons = new LinkedList<Object>();

  public static <T> WeakReference<T> registerSingleton(T singleton) {
    singletons.add(singleton);
    return new WeakReference<T>(singleton);
  }

  public static void shutdown() {
    singletons = null;
  }

}

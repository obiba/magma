package org.obiba.magma.js;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import groovy.lang.Binding;


public class MagmaContext extends Binding {
  Map<Object,LinkedList<Object>> sharedLocal = Maps.newHashMap();

  public void push(Object key, Object value) {
    if(!sharedLocal.containsKey(key)) sharedLocal.put(key, new LinkedList<>());

    sharedLocal.get(key).push(value);
  }

  public Object get(Object key) {
    return !sharedLocal.containsKey(key) ? null : sharedLocal.get(key).peek();
  }

  public void pop(Object key) {
    sharedLocal.get(key).pop();
  }

  public <T> T exec(Supplier<T> supplier) {
    return exec(supplier, Maps.newHashMap());
  }

  public <T> T exec(Supplier<T> supplier, Map<Object, Object> shared) {
    //Bindings current = getBindings(ScriptContext.ENGINE_SCOPE);
    try {
      //setBindings(new SimpleBindings(), ScriptContext.ENGINE_SCOPE);
      shared.forEach((k, v) -> push(k, v));
      return supplier.get();
    } finally {
      //setBindings(current, ScriptContext.ENGINE_SCOPE);
      shared.forEach((k, v) -> pop(k));
    }
  }
}

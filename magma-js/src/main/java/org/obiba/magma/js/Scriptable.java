package org.obiba.magma.js;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.base.Throwables;
import groovy.lang.Closure;
import groovy.lang.ExpandoMetaClass;

public abstract class Scriptable {
  protected static void addMethodProvider(ExpandoMetaClass expando, Map<String, Closure> members, Class<?> clazz) {
    for(Method m : clazz.getMethods()) {
      Closure closure = new Closure(null) {
        public Object doCall(Object... args) {
          try {
            Object thiz = this.getDelegate();
            if(thiz == null) thiz = MagmaContextFactory.getScriptableContext();
            return m.invoke(null, thiz , args);
          } catch (IllegalAccessException | InvocationTargetException e) {
            Throwables.propagateIfInstanceOf(e.getCause(), MagmaJsEvaluationRuntimeException.class);
            throw Throwables.propagate(e);
          } catch (IllegalArgumentException e) {
            throw Throwables.propagate(e);
          }
        }
      };
      expando.registerInstanceMethod(m.getName(), closure);
      members.put(m.getName(), closure);
    }
  }
}

package org.obiba.magma.js;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public abstract class AbstractPrototypeFactory {

  private final Set<String> excluded = ImmutableSet
      .of("wait", "toString", "getClass", "equals", "hashCode", "notify", "notifyAll");

  private Set<Class<?>> methodProviders = new HashSet<Class<?>>();

  public void addMethodProvider(Class<?> methodProvider) {
    methodProviders.add(methodProvider);
  }

  public Scriptable buildPrototype() {
    // Value.prototype = new Object();
    Scriptable ctor = newPrototype();

    ScriptableObject prototype = new NativeObject();
    createMethods(prototype);
    ScriptableObject.putConstProperty(ctor, "prototype", prototype);
    return ctor;
  }

  protected void createMethods(ScriptableObject so) {
    Iterable<Method> methods = Iterables
        .concat(Iterables.transform(methodProviders, new Function<Class<?>, Iterable<Method>>() {
          @Override
          public Iterable<Method> apply(Class<?> from) {
            return Iterables.filter(Arrays.asList(from.getMethods()), new Predicate<Method>() {
              @Override
              public boolean apply(Method input) {
                return excluded.contains(input.getName()) == false;
              }
            });
          }
        }));

    for(Method method : methods) {
      FunctionObject fo = new FunctionObject(method.getName(), method, so);
      so.defineProperty(method.getName(), fo, ScriptableObject.DONTENUM);
    }

  }

  protected abstract Scriptable newPrototype();
}

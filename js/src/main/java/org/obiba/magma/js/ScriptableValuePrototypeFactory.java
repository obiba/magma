package org.obiba.magma.js;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.obiba.magma.js.methods.BooleanMethods;
import org.obiba.magma.js.methods.DateTimeMethods;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * A factory class for constructing the {@code ScriptableValue} javascript prototype. This prototype defines all the
 * methods that can be invoked on {@code ScriptableValue}. These methods are provided by static methods of some Java
 * classes. Classes to inspect can be added to the factory through the {@code #addMethodProvider(Class)} method. For
 * each method of each class added to the factory, the factory will add a {@code FunctionObject} to the {@code
 * ScriptableValue}.
 * <p>
 * For example, adding the following class to the factory will result in a {@code hello()} method to be added:
 * 
 * <pre>
 * public final class HelloMethod {
 *   public static Object hello(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
 *     return &quot;Hello&quot;;
 *   }
 * }
 * </pre>
 * 
 * The contributed method can then be invoked on any {@code ScriptableValue}:
 * 
 * <pre>
 *   $('MyVar').hello()
 * </pre>
 * 
 * would evaluate to "Hello".
 * <p>
 * In order to chain these methods together, they should return {@code ScriptableValue} themselves. This allows
 * constructs like this one:
 * 
 * <pre>
 *   $('BloodPressure.Systolic').avg().round(2).greaterThan($('BloodPressure.Dyastolic').avg().round(2))
 * </pre>
 */
public class ScriptableValuePrototypeFactory {

  private final Set<String> excluded = ImmutableSet.of("wait", "toString", "getClass", "equals", "hashCode", "notify", "notifyAll");

  private Set<Class<?>> methodProviders = new HashSet<Class<?>>();

  public ScriptableValuePrototypeFactory() {
    methodProviders.add(BooleanMethods.class);
    methodProviders.add(DateTimeMethods.class);
  }

  public void addMethodProvider(Class<?> methodProvider) {
    methodProviders.add(methodProvider);
  }

  public Scriptable buildPrototype() {
    // Value.prototype = new Object();
    ScriptableValue ctor = new ScriptableValue();

    ScriptableObject prototype = new NativeObject();
    createMethods(prototype);
    ScriptableObject.putConstProperty(ctor, "prototype", prototype);
    return ctor;
  }

  protected void createMethods(ScriptableObject so) {
    Iterable<Method> methods = Iterables.concat(Iterables.transform(methodProviders, new Function<Class<?>, Iterable<Method>>() {
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
}

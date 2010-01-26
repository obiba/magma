package org.obiba.magma.js.methods;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.js.GlobalMethodProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * An implementation of {@code GlobalMethodProvider} that provides some methods from this class. Extending classes are
 * required to implement the {@code #getExposedMethods()} method by returning a {@code Set} of this class' method names
 * to be exposed in the JavaScript context. Extending classes may also rename the methods before exposing them to the
 * JavaScript engine by implementing the {@code #getMethodNameMap()} method and returning a {@code Map} of Java method
 * name to JavaScript method name.
 * 
 */
public abstract class AbstractGlobalMethodProvider implements GlobalMethodProvider {

  /**
   * This implementation will iterate on this class's methods and expose return the {@code Method} instances for which
   * the {@code name} attribute is contained in the set of method names returned by {@code #getExposedMethods()}
   */
  @Override
  public Collection<Method> getJavaScriptExtensionMethods() {
    return ImmutableList.copyOf(Iterables.filter(Arrays.asList(getClass().getMethods()), new Predicate<Method>() {
      @Override
      public boolean apply(Method input) {
        return getExposedMethods().contains(input.getName());
      }
    }));
  }

  /**
   * This implementation will use the method's name as the lookup key in the Map returned by {@code #getMethodNameMap()}
   * , if a value exists for the key, it is returned as the method's name, otherwise, the Java method's name is
   * returned.
   */
  @Override
  public String getJavaScriptMethodName(Method method) {
    if(method == null) throw new IllegalArgumentException("method cannot be null");
    String name = getMethodNameMap().get(method.getName());
    return name != null ? name : method.getName();
  }

  /**
   * Returns the {@code Set} of this class' method names to expose.
   * @return a {@code Set} of method names to expose
   */
  protected abstract Set<String> getExposedMethods();

  /**
   * Returns a {@code Map} of Java method name to JavaScript method name. This implementation returns an empty map which
   * will effectively not rename any method.
   * @return a {@code Map} of method names
   */
  protected Map<String, String> getMethodNameMap() {
    return ImmutableMap.of();
  }

}

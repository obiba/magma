package org.obiba.magma.js;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Provides global JavaScript extension methods.
 */
public interface GlobalMethodProvider {

  /**
   * Returns JavaScript extension methods.
   */
  public Collection<Method> getJavaScriptExtensionMethods();
}

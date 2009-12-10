package org.obiba.magma.js;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.ContextFactory;
import org.obiba.magma.MagmaEngineExtension;

/**
 * A {@code MagmaEngine} extension for creating manipulating variables using javascript.
 */
public class MagmaJsExtension implements MagmaEngineExtension {

  private MagmaContextFactory magmaContextFactory = new MagmaContextFactory();

  private Set<GlobalMethodProvider> globalMethodProviders;

  public void setGlobalMethodProviders(Set<GlobalMethodProvider> globalMethodProviders) {
    this.globalMethodProviders = globalMethodProviders;
  }

  public void setMagmaContextFactory(MagmaContextFactory magmaContextFactory) {
    this.magmaContextFactory = magmaContextFactory;
  }

  @Override
  public String getName() {
    return "magma-js";
  }

  @Override
  public void initialise() {
    // Set MagmaContextFactory as the global factory. We can only do this if it hasn't been done already.
    if(ContextFactory.hasExplicitGlobal() == false) {
      magmaContextFactory.setGlobalMethods(getGlobalMethods());
      ContextFactory.initGlobal(magmaContextFactory);

      // Initialise the shared scope
      magmaContextFactory.initialise();
    }

  }

  private Collection<Method> getGlobalMethods() {
    List<Method> methods = new ArrayList<Method>();
    if(globalMethodProviders != null) {
      for(GlobalMethodProvider globalMethodProvider : globalMethodProviders) {
        methods.addAll(globalMethodProvider.getJavaScriptExtensionMethods());
      }
    }
    return methods;
  }
}

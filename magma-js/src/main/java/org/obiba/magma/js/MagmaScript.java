package org.obiba.magma.js;

import groovy.lang.Binding;
import groovy.lang.MetaClass;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

public abstract class MagmaScript extends Script {

  private ThreadLocal<Binding> binding = new ThreadLocal<Binding>() {};

  @Override
  public Binding getBinding() {
    return binding.get();
  }

  @Override
  public void setBinding(Binding binding) {
    this.binding.set(binding);
  }

  public Object getProperty(String property) {
    try {
      return binding.get().getVariable(property);
    } catch (MissingPropertyException e) {
      return super.getProperty(property);
    }
  }

  public void setProperty(String property, Object newValue) {
    if ("binding".equals(property))
      setBinding((Binding) newValue);
    else if("metaClass".equals(property))
      setMetaClass((MetaClass)newValue);
    else
      binding.get().setVariable(property, newValue);
  }
}

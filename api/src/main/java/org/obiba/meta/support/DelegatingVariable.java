package org.obiba.meta.support;

import org.obiba.meta.IVariable;
import org.obiba.meta.ValueType;

public class DelegatingVariable implements IVariable {

  private IVariable delegate;

  public DelegatingVariable(IVariable variable) {
    this.delegate = variable;
  }

  public IVariable getDelegate() {
    return delegate;
  }

  public String getEntityType() {
    return delegate.getEntityType();
  }

  public String getMimeType() {
    return delegate.getMimeType();
  }

  public String getName() {
    return delegate.getName();
  }

  public String getUnit() {
    return delegate.getUnit();
  }

  public ValueType getValueType() {
    return delegate.getValueType();
  }

  @Override
  public String getAttribute(String name) {
    return delegate.getAttribute(name);
  }

  @Override
  public String getReferencedEntityType() {
    return delegate.getReferencedEntityType();
  }
}

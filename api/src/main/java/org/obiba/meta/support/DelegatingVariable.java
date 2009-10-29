package org.obiba.meta.support;

import javax.xml.namespace.QName;

import org.obiba.meta.ValueType;
import org.obiba.meta.Variable;

public class DelegatingVariable implements Variable {

  private Variable delegate;

  public DelegatingVariable(Variable variable) {
    this.delegate = variable;
  }

  public Variable getDelegate() {
    return delegate;
  }

  @Override
  public QName getQName() {
    return delegate.getQName();
  }

  @Override
  public String getCollection() {
    return delegate.getCollection();
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

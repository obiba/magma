package org.obiba.magma.support;

import org.obiba.magma.ValueSetProvider;

public abstract class AbstractValueSetProvider implements ValueSetProvider {

  private String entityType;

  protected AbstractValueSetProvider(String entityType) {
    this.entityType = entityType;
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return getEntityType().equals(entityType);
  }

}

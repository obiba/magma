package org.obiba.meta.support;

import org.obiba.meta.ValueSetProvider;

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

package org.obiba.meta.support;

import org.obiba.meta.ValueSetReferenceProvider;

public abstract class AbstractValueSetReferenceProvider implements ValueSetReferenceProvider {

  private String entityType;

  protected AbstractValueSetReferenceProvider(String entityType) {
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

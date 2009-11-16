package org.obiba.meta.support;

import org.obiba.meta.VariableEntity;

public class VariableEntityBean implements VariableEntity {

  private String entityType;

  private String entityIdentifier;

  public VariableEntityBean(String entityType, String entityIdentifier) {
    this.entityType = entityType;
    this.entityIdentifier = entityIdentifier;
  }

  @Override
  public String getIdentifier() {
    return entityIdentifier;
  }

  @Override
  public String getType() {
    return entityType;
  }

  @Override
  public String toString() {
    return "entity[" + getType() + ":" + getIdentifier() + "]";
  }
}

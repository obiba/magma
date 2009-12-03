package org.obiba.magma.support;


public abstract class AbstractVariableEntityProvider implements VariableEntityProvider {

  private String entityType;

  protected AbstractVariableEntityProvider(String entityType) {
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

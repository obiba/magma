package org.obiba.magma.support;

import javax.validation.constraints.NotNull;

public abstract class AbstractVariableEntityProvider implements VariableEntityProvider {

  @NotNull
  private final String entityType;

  protected AbstractVariableEntityProvider(@NotNull String entityType) {
    this.entityType = entityType;
  }

  @NotNull
  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public boolean isForEntityType(@SuppressWarnings("ParameterHidesMemberVariable") String entityType) {
    return getEntityType().equals(entityType);
  }

}

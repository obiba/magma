package org.obiba.magma.support;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.VariableEntity;

public interface VariableEntityProvider {

  @NotNull
  String getEntityType();

  boolean isForEntityType(String entityType);

  @NotNull
  Set<VariableEntity> getVariableEntities();
}

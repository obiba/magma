package org.obiba.magma.support;

import java.util.Set;

import org.obiba.magma.VariableEntity;

public interface VariableEntityProvider {

  String getEntityType();

  boolean isForEntityType(String entityType);

  Set<VariableEntity> getVariableEntities();
}

package org.obiba.magma.support;

import java.util.Set;

import org.obiba.magma.VariableEntity;

public interface VariableEntityProvider {

  public String getEntityType();

  public boolean isForEntityType(String entityType);

  public Set<VariableEntity> getVariableEntities();
}

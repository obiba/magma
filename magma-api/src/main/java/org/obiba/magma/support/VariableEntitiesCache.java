package org.obiba.magma.support;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;

public class VariableEntitiesCache implements Serializable {

  private static final long serialVersionUID = 69918333951801112L;

  private Set<VariableEntity> entities;

  private long lastUpdate;

  public VariableEntitiesCache(Set<VariableEntity> entities, Value lastUpdate) {
    this(entities, ((Date)lastUpdate.getValue()).getTime());
  }

  public VariableEntitiesCache(Set<VariableEntity> entities, long lastUpdate) {
    this.entities = entities;
    this.lastUpdate = lastUpdate;
  }

  public boolean isUpToDate(Value updated) {
    return lastUpdate == ((Date)updated.getValue()).getTime();
  }

  public Set<VariableEntity> getEntities() {
    return entities;
  }
}

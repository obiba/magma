package org.obiba.magma.datasource.mongodb;

import java.util.Set;

import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityProvider;

class MongoDBVariableEntityProvider implements VariableEntityProvider {

  private String entityType;

  private final MongoDBValueTable table;

  MongoDBVariableEntityProvider(MongoDBValueTable table, String entityType) {
    this.table = table;
    this.entityType = entityType;
  }

  @Override
  public String getEntityType() {
    if (entityType == null) {
      entityType = (String) table.asDBObject().get("entityType");
    }
    return entityType;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Set<VariableEntity> getVariableEntities() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}

/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;

import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

class MongoDBVariableEntityProvider implements VariableEntityProvider {

  private String entityType;

  private final MongoDBValueTable table;

  private Set<VariableEntity> cachedEntities;

  private Value lastUpdated;

  MongoDBVariableEntityProvider(MongoDBValueTable table, String entityType) {
    this.table = table;
    this.entityType = entityType;
  }

  @NotNull
  @Override
  public String getEntityType() {
    if(entityType == null) {
      entityType = (String) table.asDBObject().get("entityType");
    }
    return entityType;
  }

  @Override
  public boolean isForEntityType(@SuppressWarnings("ParameterHidesMemberVariable") String entityType) {
    return getEntityType().equals(entityType);
  }

  @NotNull
  @Override
  public Set<VariableEntity> getVariableEntities() {
    Value tableLastUpdate = table.getTimestamps().getLastUpdate();
    if(cachedEntities == null || lastUpdated == null || !lastUpdated.equals(tableLastUpdate)) {
      lastUpdated = tableLastUpdate;
      cachedEntities = loadEntities();
    }
    return cachedEntities;
  }

  private Set<VariableEntity> loadEntities() {
    ImmutableSet.Builder<VariableEntity> builder = ImmutableSet.builder();
    try(DBCursor cursor = table.getValueSetCollection().find(new BasicDBObject())) {
      while(cursor.hasNext()) {
        builder.add(new VariableEntityBean(getEntityType(), cursor.next().get("_id").toString()));
      }
    }
    return builder.build();
  }
}

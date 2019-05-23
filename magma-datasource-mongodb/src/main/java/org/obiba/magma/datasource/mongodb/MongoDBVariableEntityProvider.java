/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.obiba.magma.Value;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.lang.VariableEntityList;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;

import javax.validation.constraints.NotNull;
import java.util.List;

class MongoDBVariableEntityProvider implements VariableEntityProvider {

  private String entityType;

  private final MongoDBValueTable table;

  private List<VariableEntity> cachedEntities;

  private Value lastUpdated;

  MongoDBVariableEntityProvider(MongoDBValueTable table, String entityType) {
    this.table = table;
    this.entityType = entityType;
  }

  @NotNull
  @Override
  public String getEntityType() {
    if (entityType == null) {
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
  public synchronized List<VariableEntity> getVariableEntities() {
    Value tableLastUpdate = table.getTimestamps().getLastUpdate();
    if (cachedEntities == null || lastUpdated == null || !lastUpdated.equals(tableLastUpdate)) {
      lastUpdated = tableLastUpdate;
      cachedEntities = loadEntities();
    }
    return cachedEntities;
  }

  private List<VariableEntity> loadEntities() {
    List<VariableEntity> list = new VariableEntityList();
    try (DBCursor cursor = table.getValueSetCollection().find(new BasicDBObject(), BasicDBObjectBuilder.start("_id", 1).get())) {
      while (cursor.hasNext()) {
        DBObject next = cursor.next();
        list.add(new VariableEntityBean(getEntityType(), next.get("_id").toString()));
      }
    }
    return list;
  }
}

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

import com.mongodb.*;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.lang.VariableEntityList;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

class MongoDBVariableEntityProvider implements VariableEntityProvider {

  private static final Logger log = LoggerFactory.getLogger(MongoDBVariableEntityProvider.class);

  private String entityType;

  private final MongoDBValueTable table;

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
    log.debug("Querying all entities from MongoDB table {}!", table.getName());
    return getVariableEntities(0, -1);
  }

  public List<VariableEntity> getVariableEntities(int offset, int limit) {
    DBCollection collection = table.getValueSetCollection();
    int total = (int) collection.count();
    int from = Math.max(offset, 0);
    from = Math.min(from, total);
    int pageSize = limit < 0 ? total : limit;

    List<VariableEntity> list = new VariableEntityList();
    try (DBCursor cursor = collection.find(new BasicDBObject(), BasicDBObjectBuilder.start("_id", 1).get()).skip(from).limit(pageSize)) {
      while (cursor.hasNext()) {
        DBObject next = cursor.next();
        list.add(new VariableEntityBean(getEntityType(), next.get("_id").toString()));
      }
    }
    return list;
  }
}

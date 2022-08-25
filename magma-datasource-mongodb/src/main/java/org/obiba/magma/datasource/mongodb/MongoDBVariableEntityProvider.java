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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.lang.VariableEntityList;
import org.obiba.magma.support.PagingVariableEntityProvider;
import org.obiba.magma.support.VariableEntityBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.NoSuchElementException;

class MongoDBVariableEntityProvider implements PagingVariableEntityProvider {

  private static final Logger log = LoggerFactory.getLogger(MongoDBVariableEntityProvider.class);

  private String entityType;

  private final MongoDBValueTable table;

  private final Bson idProjection = Projections.include("_id");

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

  @Override
  public List<VariableEntity> getVariableEntities(int offset, int limit) {
    MongoCollection<Document> collection = table.getValueSetCollection();
    int total = (int) collection.countDocuments();
    int from = Math.max(offset, 0);
    from = Math.min(from, total);
    int pageSize = limit < 0 ? total : limit;

    List<VariableEntity> list = new VariableEntityList();
    try (MongoCursor<Document> cursor = collection.find().projection(idProjection).skip(from).limit(pageSize).cursor()) {
      while (true) {
        Document next = cursor.next();
        list.add(new VariableEntityBean(getEntityType(), next.get("_id").toString()));
      }
    } catch (NoSuchElementException e) {
      // ignored, reading cursor is finished
    }
    return list;
  }

  @Override
  public boolean hasVariableEntity(VariableEntity entity) {
    Document doc = table.getValueSetCollection()
        .find(Filters.eq("_id", entity.getIdentifier()))
        .projection(idProjection)
        .first();
    return doc != null;
  }

  @Override
  public int getVariableEntityCount() {
    return (int) table.getValueSetCollection().countDocuments();
  }
}

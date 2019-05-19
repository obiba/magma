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

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.bson.BSONObject;
import org.obiba.magma.VariableEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Extract the raw value set document(s) from MongoDB database.
 */
class MongoDBValueSetFetcher {

  private final MongoDBValueTable table;

  public MongoDBValueSetFetcher(MongoDBValueTable table) {
    this.table = table;
  }

  BSONObject getDBObject(VariableEntity entity) {
    DBObject template = BasicDBObjectBuilder.start("_id", entity.getIdentifier()).get();
    BSONObject object = table.getValueSetCollection().findOne(template);
    return object;
  }

  DBCursor getDBObjects(List<VariableEntity> entities) {
    DBObject query = QueryBuilder.start("_id").in(entities.stream().map(e -> e.getIdentifier()).collect(Collectors.toList())).get();
    return table.getValueSetCollection().find(query);
  }
}

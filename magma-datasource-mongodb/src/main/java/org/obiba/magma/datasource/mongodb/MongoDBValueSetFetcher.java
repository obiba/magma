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

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
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

  Document getDBObject(VariableEntity entity) {
    Document object = table.getValueSetCollection().find(Filters.eq("_id", entity.getIdentifier())).first();
    return object;
  }

  MongoCursor<Document> getDBObjects(List<VariableEntity> entities) {
    return table.getValueSetCollection().find(Filters.in("_id",
        entities.stream().map(VariableEntity::getIdentifier).collect(Collectors.toList()))).cursor();
  }
}

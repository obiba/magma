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
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSetBatch;
import org.obiba.magma.VariableEntity;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Get the list of raw value set documents from MongoDB database in one query and build corresponding {@link MongoDBValueSet}s.
 */
public class MongoDBValueSetBatch implements ValueSetBatch {

  private final MongoDBValueTable table;

  private final Map<String, VariableEntity> entitiesMap;

  private final MongoDBValueSetFetcher fetcher;

  public MongoDBValueSetBatch(MongoDBValueTable table, List<VariableEntity> entities) {
    this.table = table;
    this.fetcher = new MongoDBValueSetFetcher(table);
    this.entitiesMap = entities.stream().collect(Collectors.toMap(VariableEntity::getIdentifier, Function.identity()));
  }

  @Override
  public List<ValueSet> getValueSets() {
    ImmutableList.Builder<ValueSet> builder = ImmutableList.builder();
    try (MongoCursor<Document> cursor = fetcher.getDBObjects(Lists.newArrayList(entitiesMap.values()))) {
      while (cursor.hasNext()) {
        Document object = cursor.next();
        String identifier = object.get("_id").toString();
        MongoDBValueSet vs = new MongoDBValueSet(table, entitiesMap.get(identifier));
        vs.setDBObject(object);
        builder.add(vs);
      }
    } catch(NoSuchElementException e) {
      // ignored, reading is finished
    }
    return builder.build();
  }
}

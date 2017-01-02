/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.datasource.mongodb;

import java.util.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.obiba.magma.*;
import org.obiba.magma.datasource.mongodb.converter.ValueConverter;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.type.DateTimeType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;

public class MongoDBValueTable extends AbstractValueTable {

  private static final String VARIABLE_SUFFIX = "_variable";

  private static final String VALUE_SET_SUFFIX = "_value_set";

  private DBObject dbObject;

  public MongoDBValueTable(@NotNull Datasource datasource, @NotNull String name) {
    this(datasource, name, null);
  }

  public MongoDBValueTable(@NotNull Datasource datasource, @NotNull String name, @Nullable String entityType) {
    super(datasource, name);
    setVariableEntityProvider(new MongoDBVariableEntityProvider(this, entityType));
    // ensure corresponding document is stored
    asDBObject();
  }

  MongoDBFactory getMongoDBFactory() {
    return getMongoDBDatasource().getMongoDBFactory();
  }

  DBCollection getValueTableCollection() {
    return getMongoDBDatasource().getValueTableCollection();
  }

  DBCollection getVariablesCollection() {
    return getMongoDBFactory().execute(new MongoDBFactory.MongoDBCallback<DBCollection>() {
      @Override
      public DBCollection doWithDB(DB db) {
        DBCollection collection = db.getCollection(getId() + VARIABLE_SUFFIX);
        collection.createIndex("name");
        return collection;
      }
    });
  }

  DBCollection getValueSetCollection() {
    return getMongoDBFactory().execute(new MongoDBFactory.MongoDBCallback<DBCollection>() {
      @Override
      public DBCollection doWithDB(DB db) {
        return db.getCollection(getId() + VALUE_SET_SUFFIX);
      }
    });
  }

  DBObject asDBObject() {
    if(dbObject == null) {
      dbObject = getValueTableCollection().findOne(BasicDBObjectBuilder.start() //
          .add("datasource", getMongoDBDatasource().asDBObject().get("_id")) //
          .add("name", getName()) //
          .get());
      // create DBObject if not found
      if(dbObject == null) {
        dbObject = BasicDBObjectBuilder.start() //
            .add("datasource", getMongoDBDatasource().asDBObject().get("_id")) //
            .add("name", getName()) //
            .add("entityType", getEntityType()) //
            .add(MongoDBDatasource.TIMESTAMPS_FIELD, MongoDBDatasource.createTimestampsObject()).get();
        getValueTableCollection().insert(dbObject, WriteConcern.ACKNOWLEDGED);
      }
    }
    return dbObject;
  }

  void setLastUpdate(Date date) {
    ((BSONObject) asDBObject().get(MongoDBDatasource.TIMESTAMPS_FIELD)).put("updated", date);
    getValueTableCollection().save(asDBObject());
    getMongoDBDatasource().setLastUpdate(date);
  }

  private String getId() {
    return asDBObject().get("_id").toString();
  }

  private ObjectId getIdAsObjectId() {
    return new ObjectId(getId());
  }

  @Override
  protected void addVariableValueSource(VariableValueSource source) {
    super.addVariableValueSource(source);
  }

  @Override
  protected void removeVariableValueSource(String variableName) {
    super.removeVariableValueSource(variableName);
  }

  @Override
  public void initialise() {
    addVariableValueSources(new MongoDBVariableValueSourceFactory(this));
    super.initialise();
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    if(!hasValueSet(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }
    return super.getValueSetTimestamps(entity);
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(final SortedSet<VariableEntity> entities) {

    if(entities.isEmpty()) {
      return ImmutableList.of();
    }
    return new Iterable<Timestamps>() {
      @Override
      public Iterator<Timestamps> iterator() {
        return new TimestampsIterator(entities.iterator());
      }
    };
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if(!hasValueSet(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }
    return new MongoDBValueSet(this, entity);
  }

  @Override
  protected ValueSetBatch getValueSetsBatch(List<VariableEntity> entities) {
    return new MongoDBValueSetBatch(this, entities);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      private final BSONObject timestampsObject = (BSONObject) asDBObject().get(MongoDBDatasource.TIMESTAMPS_FIELD);

      @NotNull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(timestampsObject.get(MongoDBDatasource.TIMESTAMPS_UPDATED_FIELD));
      }

      @NotNull
      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(timestampsObject.get(MongoDBDatasource.TIMESTAMPS_CREATED_FIELD));
      }
    };
  }

  public void drop() {
    // drop associated collections
    dropFiles();
    getValueSetCollection().drop();
    getVariablesCollection().drop();
    getValueTableCollection().remove(BasicDBObjectBuilder.start().add("_id", getIdAsObjectId()).get());
    getMongoDBDatasource().setLastUpdate(new Date());
    dbObject = null;
  }

  DBObject findVariable(String variableName) {
    return getVariablesCollection().findOne(BasicDBObjectBuilder.start("name", variableName).get());
  }

  @Override
  public int getVariableCount() {
    return (int) getVariablesCollection().count();
  }

  @Override
  public int getValueSetCount() {
    return (int) getValueSetCollection().count();
  }

  @Override
  public int getVariableEntityCount() {
    return (int) getValueSetCollection().count();
  }

  @Override
  public boolean canDropValueSets() {
    return true;
  }

  @Override
  public void dropValueSets() {
    dropFiles();
    getValueSetCollection().drop();
    setLastUpdate(new Date());
  }

  private MongoDBDatasource getMongoDBDatasource() {
    return ((MongoDBDatasource) getDatasource());
  }

  /**
   * Drop the files from the {@link com.mongodb.gridfs.GridFS} for this table.
   */
  private void dropFiles() {
    GridFS gridFS = getMongoDBDatasource().getMongoDBFactory().getGridFS();
    BasicDBObjectBuilder metaDataQuery = BasicDBObjectBuilder.start() //
        .add("metadata.datasource", getDatasource().getName()) //
        .add("metadata.table", getName());
    gridFS.remove(metaDataQuery.get());
  }

  private class TimestampsIterator implements Iterator<Timestamps> {

    private final Iterator<VariableEntity> entities;

    private final DBCursor cursor;

    private final Map<String, Timestamps> timestampsMap = Maps.newHashMap();

    private TimestampsIterator(Iterator<VariableEntity> entities) {
      this.entities = entities;
      DBObject fields = BasicDBObjectBuilder.start(MongoDBDatasource.TIMESTAMPS_FIELD, 1).get();
      cursor = getValueSetCollection().find(new BasicDBObject(), fields);
    }

    @Override
    public boolean hasNext() {
      return entities.hasNext();
    }

    @Override
    public Timestamps next() {
      VariableEntity entity = entities.next();

      if(timestampsMap.containsKey(entity.getIdentifier())) return getTimestampsFromMap(entity);

      boolean found = false;
      while(cursor.hasNext() && !found) {
        DBObject obj = cursor.next();
        String id = obj.get("_id").toString();
        BSONObject timestamps = (BSONObject) obj.get(MongoDBDatasource.TIMESTAMPS_FIELD);
        timestampsMap.put(id,
            new TimestampsBean(ValueConverter.unmarshall(DateTimeType.get(), timestamps.get("created")),
                ValueConverter.unmarshall(DateTimeType.get(), timestamps.get("updated")))
        );
        found = id.equals(entity.getIdentifier());
      }

      if(timestampsMap.containsKey(entity.getIdentifier())) return getTimestampsFromMap(entity);
      return NullTimestamps.get();
    }

    /**
     * No duplicate of entities, so remove timestamps from map once get.
     *
     * @param entity
     * @return
     */
    private Timestamps getTimestampsFromMap(VariableEntity entity) {
      Timestamps value = timestampsMap.get(entity.getIdentifier());
      timestampsMap.remove(entity.getIdentifier());
      return value;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}

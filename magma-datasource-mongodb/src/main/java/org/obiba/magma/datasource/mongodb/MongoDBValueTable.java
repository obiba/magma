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

import java.util.Date;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.type.DateTimeType;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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
    return ((MongoDBDatasource) getDatasource()).getMongoDBFactory();
  }

  DB getDB() {
    return getMongoDBFactory().getDB();
  }

  GridFS getGridFS() {
    return getMongoDBFactory().getGridFS();
  }

  DBCollection getValueTableCollection() {
    return ((MongoDBDatasource) getDatasource()).getValueTableCollection();
  }

  DBCollection getVariablesCollection() {
    DBCollection collection = getDB().getCollection(getId() + VARIABLE_SUFFIX);
    collection.ensureIndex("name");
    return collection;
  }

  DBCollection getValueSetCollection() {
    return getDB().getCollection(getId() + VALUE_SET_SUFFIX);
  }

  DBObject asDBObject() {
    if(dbObject == null) {
      dbObject = getValueTableCollection().findOne(BasicDBObjectBuilder.start() //
          .add("datasource", getDatasource().getName()) //
          .add("name", getName()) //
          .get());
      // create DBObject if not found
      if(dbObject == null) {
        dbObject = BasicDBObjectBuilder.start() //
            .add("datasource", getDatasource().getName()) //
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
    ((MongoDBDatasource) getDatasource()).setLastUpdate(date);
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
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if(!hasValueSet(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }
    return new MongoDBValueSet(this, entity);
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
    getValueSetCollection().drop();
    getVariablesCollection().drop();
    getValueTableCollection().remove(BasicDBObjectBuilder.start().add("_id", getIdAsObjectId()).get());
    ((MongoDBDatasource) getDatasource()).setLastUpdate(new Date());
    dbObject = null;
  }

  DBObject findVariable(String variableName) {
    return getVariablesCollection().findOne(BasicDBObjectBuilder.start("name", variableName).get());
  }

}

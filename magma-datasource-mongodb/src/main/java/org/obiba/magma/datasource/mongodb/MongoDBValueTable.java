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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.BSONObject;
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

  public MongoDBValueTable(@Nonnull Datasource datasource, @Nonnull String name) {
    this(datasource, name, null);
  }

  public MongoDBValueTable(@Nonnull Datasource datasource, @Nonnull String name, @Nullable String entityType) {
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

  DBCollection getDatasourceCollection() {
    return ((MongoDBDatasource) getDatasource()).getDatasourceCollection();
  }

  DBCollection getVariablesCollection() {
    return getDB().getCollection(getId() + VARIABLE_SUFFIX);
  }

  DBCollection getValueSetCollection() {
    return getDB().getCollection(getId() + VALUE_SET_SUFFIX);
  }

  DBObject getDatasourceDBObject() {
    return ((MongoDBDatasource) getDatasource()).asDBObject();
  }

  DBObject asDBObject() {
    DBObject tableObject = getValueTableCollection().findOne(BasicDBObjectBuilder.start() //
        .add("_id", getId()) //
        .get());

    if(tableObject == null) {
      tableObject = BasicDBObjectBuilder.start() //
          .add("_id", getId()) //
          .add("datasource", getDatasource().getName()) //
          .add("name", getName()) //
          .add("entityType", getEntityType()) //
          .add(MongoDBDatasource.TIMESTAMPS_FIELD, MongoDBDatasource.createTimestampsObject()).get();
      getValueTableCollection().insert(tableObject, WriteConcern.ACKNOWLEDGED);
    }

    return tableObject;
  }

  void setLastUpdate(Date date) {
    DBObject tableObject = asDBObject();
    BSONObject timestamps = (BSONObject) tableObject.get(MongoDBDatasource.TIMESTAMPS_FIELD);
    timestamps.put("updated", date);
    getValueTableCollection().save(tableObject);
    ((MongoDBDatasource) getDatasource()).setLastUpdate(date);
  }

  /**
   * See <a href="http://docs.mongodb.org/manual/reference/limits/">MongoDB Limits and Thresholds</a>.
   *
   * @return
   */
  private String getId() {
    String norm = (getDatasource().getName() + "." + getName()).replaceAll("[$]", "_");
    return norm.startsWith("system") ? "_" + norm.substring(6) : norm;
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

  @Nonnull
  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {

      private BSONObject getTimestampsObject() {
        return (BSONObject) asDBObject().get(MongoDBDatasource.TIMESTAMPS_FIELD);
      }

      @Nonnull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(getTimestampsObject().get(MongoDBDatasource.TIMESTAMPS_UPDATED_FIELD));
      }

      @Nonnull
      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(getTimestampsObject().get(MongoDBDatasource.TIMESTAMPS_CREATED_FIELD));
      }
    };
  }

  public void drop() {
    // drop associated collections
    getValueSetCollection().drop();
    getVariablesCollection().drop();
    getValueTableCollection().remove(BasicDBObjectBuilder.start().add("_id", getId()).get());
    ((MongoDBDatasource) getDatasource()).setLastUpdate(new Date());
  }

}
